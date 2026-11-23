# Questions 9, 10 et 11 : architecture agentique

```
 Angular (4200)   Telegram   Discord          ← Q10 / Q11
      │  \            \        /
      │   └──────── agent-service (8087) ──── Ollama (11434, LLM local)   ← Q9
      │                 │  client MCP (Streamable HTTP /mcp)
      │      ┌──────────┼───────────────┐
      ▼      ▼          ▼               ▼
  gateway  customer   inventory      billing      ← serveurs MCP (@McpTool)
  (8888)   (8081)     (8082)         (8083)
           └── Eureka (8761) + config-service (9999) ──┘
```

## Ce qui a été ajouté ou modifié

| Fichier | Rôle |
|---|---|
| `*/pom.xml` | Java 17 → **21** (obligatoire pour Spring AI 2.0) + starter `spring-ai-starter-mcp-server-webmvc` et BOM `spring-ai-bom 2.0.2` dans customer, inventory et billing |
| `customer-service/.../mcp/CustomerMcpTools.java` | Outils MCP : `getAllCustomers`, `getCustomerById`, `searchCustomersByName`, `createCustomer` |
| `inventory-service/.../mcp/InventoryMcpTools.java` | Outils MCP : `getAllProducts`, `getProductById`, `searchProductsByName`, `getLowStockProducts` |
| `billing-service/.../mcp/BillingMcpTools.java` | Outils MCP : `getAllBills`, `getBillById`, `getBillsByCustomerId`, `getBillTotal` |
| `billing-service/.../service/BillService.java` | Logique « facture complète » (Feign), partagée entre REST et MCP |
| `billing-service/.../web/BillRestController.java` | + `/api/fullBills` et `/api/fullBills/{id}` pour Angular |
| `*/application.properties` | `spring.ai.mcp.server.*` → chaque service expose `http://localhost:<port>/mcp` |
| `gateway-service/application.properties` | CORS pour `http://localhost:4200` |
| **`agent-service/`** (nouveau) | Agent IA : Ollama + client MCP + mémoire + API REST + bots Telegram et Discord |
| **`ecom-web/`** (nouveau) | Client Angular : Clients, Produits, Factures, Assistant IA |

## 1. Prérequis

1. **JDK 21**. Dans IntelliJ : *File > Project Structure > SDK = 21*, puis recharger Maven pour chaque module.
2. **Ollama** (gratuit) : <https://ollama.com/download>, puis :
   ```bash
   ollama pull qwen2.5:7b
   ```
   Le modèle doit supporter le *tool calling*. Sur un PC modeste (8 Go de RAM), prendre `qwen2.5:3b` et modifier `spring.ai.ollama.chat.model` dans `agent-service/src/main/resources/application.properties`.
3. **Node.js 20+** pour Angular.

## 2. Ordre de démarrage (important)

1. `config-service` (9999)
2. `discovery-service` (8761)
3. `customer-service` (8081), `inventory-service` (8082), puis `billing-service` (8083)
4. `gateway-service` (8888)
5. **`agent-service` (8087) en dernier** : au démarrage, il se connecte aux 3 serveurs MCP. Si l'un d'eux est éteint, l'agent ne démarre pas.

Tests rapides :
- Agent : <http://localhost:8087/chat?query=Quels sont les clients ?>
- Logs de l'agent au démarrage : on doit voir les 3 connexions MCP initialisées.

## 3. Q10 : bots Telegram et Discord

Les bots démarrent **uniquement si leur token est défini**. Dans IntelliJ, *Run > Edit Configurations > agent-service > Environment variables* :

```
TELEGRAM_BOT_TOKEN=123456:ABC...;DISCORD_BOT_TOKEN=MTE...
```

**Telegram**
1. Dans Telegram, ouvrir **@BotFather** → `/newbot` → choisir un nom → copier le token.
2. Démarrer agent-service, ouvrir le bot dans Telegram et envoyer `/start`, puis une question.

**Discord**
1. Sur <https://discord.com/developers/applications> : *New Application* → onglet *Bot* → *Reset Token* et copier le token.
2. Dans le même onglet *Bot*, activer **MESSAGE CONTENT INTENT** (sinon le bot ne lit pas les messages).
3. Dans *OAuth2 > URL Generator* : cocher le scope `bot`, puis les permissions *Send Messages* et *Read Message History*. Ouvrir l'URL générée et inviter le bot sur ton serveur.
4. Usage : message privé au bot, ou dans un salon `@NomDuBot quels produits sont en rupture ?`, ou `!ask ...`.

Chaque conversation (chat Telegram, salon Discord, onglet web) a sa **propre mémoire**.

## 4. Q11 : client Angular

```bash
cd ecom-web
npm install
npm start          # http://localhost:4200
```

- Les pages **Clients / Produits / Factures** passent par la **gateway** (`http://localhost:8888/CUSTOMER-SERVICE/api/customers`, etc.), qui utilise les routes dynamiques via Eureka.
- La page **Assistant IA** appelle directement `agent-service` (`POST http://localhost:8087/chat`).
- Les URL se règlent dans `ecom-web/src/app/config.ts`.

## 5. Questions à tester

- « Quels sont les clients ? »
- « Quels produits ont moins de 10 unités en stock ? »
- « Quelles sont les factures de Imane ? » (l'agent enchaîne `searchCustomersByName` puis `getBillsByCustomerId`)
- « Quel est le montant total de la facture 1 ? »
- « Ajoute un client Sara avec l'email sara@gmail.com »

## 6. Dépannage

| Symptôme | Cause / solution |
|---|---|
| `invalid target release: 21` | Le JDK du projet est encore 17 : passer au JDK 21 |
| agent-service ne démarre pas, erreur de connexion MCP | Un des 3 services est éteint : les démarrer avant l'agent |
| L'agent répond sans appeler d'outil, ou invente | Le modèle Ollama ne gère pas les outils : utiliser `qwen2.5` ou `llama3.1` |
| `Connection refused localhost:11434` | Ollama n'est pas lancé (`ollama serve`) |
| Angular : erreur CORS sur la gateway | Redémarrer la gateway (la config CORS est dans son `application.properties`) |
| Bot Discord muet | MESSAGE CONTENT INTENT non activé |
| Timeout sur une question | Premier appel lent (le modèle se charge en mémoire) : réessayer |

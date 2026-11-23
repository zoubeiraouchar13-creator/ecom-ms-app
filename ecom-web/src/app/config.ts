/**
 * URLs du backend.
 * - GATEWAY_URL : Spring Cloud Gateway (routes dynamiques via Eureka : /NOM-DU-SERVICE/...)
 * - AGENT_URL   : agent-service (agent IA), appelé directement
 */
export const GATEWAY_URL = 'http://localhost:8888';
export const AGENT_URL = 'http://localhost:8087';

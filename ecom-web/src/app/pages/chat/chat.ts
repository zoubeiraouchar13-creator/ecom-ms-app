import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AgentApi } from '../../services/agent';
import { ChatMessage } from '../../models';

@Component({
  selector: 'app-chat',
  imports: [FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat {
  private agent = inject(AgentApi);
  private scroller = viewChild<ElementRef<HTMLDivElement>>('scroller');

  messages = signal<ChatMessage[]>([
    { from: 'agent', text: 'Bonjour 👋 Je suis l’assistant e-commerce. Je peux consulter les clients, les produits, le stock et les factures.' },
  ]);
  question = '';
  loading = signal(false);

  suggestions = [
    'Quels sont les clients ?',
    'Quels produits ont moins de 10 unités en stock ?',
    'Quelles sont les factures de Imane ?',
    'Quel est le montant total de la facture 1 ?',
  ];

  send(text: string = this.question): void {
    const q = text.trim();
    if (!q || this.loading()) return;
    this.messages.update((m) => [...m, { from: 'user', text: q }]);
    this.question = '';
    this.loading.set(true);
    this.scrollDown();

    this.agent.ask(q).subscribe({
      next: (answer) => this.push({ from: 'agent', text: answer }),
      error: (err) =>
        this.push({
          from: 'error',
          text: typeof err.error === 'string' && err.error ? err.error
            : 'Agent injoignable : vérifiez que agent-service (port 8087) et Ollama sont démarrés.',
        }),
    });
  }

  reset(): void {
    this.agent.resetConversation();
    this.messages.set([{ from: 'agent', text: 'Nouvelle conversation. Que puis-je faire pour vous ?' }]);
  }

  /** Mise en forme minimale du markdown renvoyé par le LLM (gras + listes). */
  format(text: string): string {
    const escaped = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return escaped
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/^\s*[-*] /gm, '• ');
  }

  private push(msg: ChatMessage): void {
    this.messages.update((m) => [...m, msg]);
    this.loading.set(false);
    this.scrollDown();
  }

  private scrollDown(): void {
    setTimeout(() => {
      const el = this.scroller()?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }
}

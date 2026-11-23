import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { AGENT_URL } from '../config';

/** Client de l'agent IA (agent-service, endpoint POST /chat). */
@Injectable({ providedIn: 'root' })
export class AgentApi {
  private http = inject(HttpClient);
  // un identifiant de conversation par onglet : l'agent garde la mémoire de l'échange
  private conversationId = crypto.randomUUID();

  ask(message: string): Observable<string> {
    return this.http
      .post<{ answer: string }>(`${AGENT_URL}/chat`, {
        conversationId: this.conversationId,
        message,
      })
      .pipe(map((res) => res.answer));
  }

  resetConversation(): void {
    this.conversationId = crypto.randomUUID();
  }
}

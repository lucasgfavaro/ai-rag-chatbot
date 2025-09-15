import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../service/chatbot.service';
import { PreserveLineBreaksPipe } from '../pipes/preserve-line-breaks.pipe';

interface ChatMessage {
  content: string;
  type: 'user' | 'bot';
  timestamp: Date;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, PreserveLineBreaksPipe],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent {
  messages: ChatMessage[] = [];
  question: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private chatbotService: ChatbotService) {}

  sendQuestion() {
    if (!this.question.trim()) return;

    // Agregar mensaje del usuario
    this.messages.push({
      content: this.question,
      type: 'user',
      timestamp: new Date()
    });

    const currentQuestion = this.question;
    this.question = '';
    this.isLoading = true;
    this.errorMessage = '';

    // Obtener respuesta del chatbot
    this.chatbotService.askQuestion(currentQuestion).subscribe({
      next: (response) => {
        this.messages.push({
          content: response.answer,
          type: 'bot',
          timestamp: new Date()
        });
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Lo siento, ocurrió un error al procesar tu pregunta.';
        this.isLoading = false;
        console.error('Error:', error);
      }
    });
  }

  trackByIndex(index: number): number {
    return index;
  }
}

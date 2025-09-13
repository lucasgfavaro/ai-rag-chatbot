import { Component } from '@angular/core';
import { ChatbotService, QuestionAnswer } from '../service/chatbot.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent {
  question: string = '';
  answer: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private chatbotService: ChatbotService) {}

  sendQuestion() {
    if (!this.question.trim()) {
      this.errorMessage = 'La pregunta no puede estar vacía.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    this.chatbotService.askQuestion(this.question).subscribe({
      next: (res: QuestionAnswer) => {
        this.answer = res.answer;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Error al obtener la respuesta.';
        this.isLoading = false;
      }
    });
  }
}

import { Component, signal } from '@angular/core';
import { DocumentUploadComponent } from './document-upload/document-upload.component';
import { ChatbotComponent } from './chatbot/chatbot.component';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [DocumentUploadComponent, ChatbotComponent, HttpClientModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('ai-rag-chatbot');
}

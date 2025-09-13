import { Component } from '@angular/core';
import { ChatbotService } from '../service/chatbot.service';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-document-upload',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './document-upload.component.html',
  styleUrls: ['./document-upload.component.css']
})
export class DocumentUploadComponent {
  uploadMessage: string = '';
  isLoading: boolean = false;

  constructor(private chatbotService: ChatbotService) {}

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.isLoading = true;
      this.chatbotService.ingestDocument(file).subscribe({
        next: (res: any) => {
          this.uploadMessage = 'Documento subido correctamente.';
          this.isLoading = false;
        },
        error: () => {
          this.uploadMessage = 'Error al subir el documento.';
          this.isLoading = false;
        }
      });
    }
  }
}

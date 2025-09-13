import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface QuestionAnswer {
  question: string;
  answer: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiUrl = 'http://localhost:8080/rag-chatbot';

  constructor(private http: HttpClient) {}

  ingestDocument(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<string>(`${this.apiUrl}/data-ingest/document`, formData);
  }

  askQuestion(question: string): Observable<QuestionAnswer> {
    const params = new HttpParams().set('question', question);
    return this.http.post<QuestionAnswer>(`${this.apiUrl}/chatbot/question`, null, { params });
  }
}

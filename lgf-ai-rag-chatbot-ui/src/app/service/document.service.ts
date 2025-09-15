import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Document {
  id: string;
  name: string;
  contentType: string;
  size: number;
  ingestedAt: string;
  content?: string;
  chunks?: DocumentChunk[];
}

export interface DocumentChunk {
  id: string;
  documentName: string;
  text: string;
  lineNumber: number;
  metadata: any;
  documentId: string;
}

export interface PageDocument {
  totalPages: number;
  totalElements: number;
  size: number;
  content: Document[];
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private baseUrl = 'http://localhost:8080/rag-chatbot';

  constructor(private http: HttpClient) {}

  getDocuments(page: number = 0, size: number = 10, name?: string): Observable<PageDocument> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (name) {
      params = params.set('name', name);
    }
    return this.http.get<PageDocument>(`${this.baseUrl}/documents`, { params });
  }

  deleteDocument(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/documents/${id}`);
  }

  uploadDocument(file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Document>(`${this.baseUrl}/data-ingest/document`, formData);
  }
}

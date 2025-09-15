import { Component, OnInit, signal, ViewChild, TemplateRef } from '@angular/core';
import { ChatbotComponent } from './chatbot/chatbot.component';
import { HttpClientModule } from '@angular/common/http';
import { CommonModule, DatePipe, JsonPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { DocumentService, Document, DocumentChunk, PageDocument } from './service/document.service';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    ChatbotComponent,
    HttpClientModule,
    CommonModule,
    MatButtonModule,
    MatListModule,
    MatPaginatorModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatFormFieldModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('ai-rag-chatbot');

  documents: Document[] = [];
  selectedDocument: Document | null = null;
  selectedDocumentChunks: DocumentChunk[] = [];

  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;

  @ViewChild('uploadModal') uploadModalRef!: TemplateRef<any>;
  dialogRef: MatDialogRef<any> | null = null;
  selectedUploadDocument: File | null = null;

  showDeleteModal: boolean = false;
  documentToDelete: Document | null = null;

  constructor(private documentService: DocumentService, private dialog: MatDialog) {}

  ngOnInit() {
    this.loadDocuments();
  }

  loadDocuments(page: number = 0) {
    this.loading = true;
    this.documentService.getDocuments(page, this.size).subscribe({
      next: (res: PageDocument) => {
        this.documents = res.content;
        this.page = res.number;
        this.size = res.size;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  selectDocument(document: Document) {
    this.selectedDocument = document;
    this.selectedDocumentChunks = document.chunks || [];
  }

  deleteDocument(document: Document) {
    if (!confirm('¿Seguro que deseas eliminar este documento?')) return;
    this.documentService.deleteDocument(document.id).subscribe({
      next: () => {
        this.loadDocuments(this.page);
        this.selectedDocument = null;
        this.selectedDocumentChunks = [];
      }
    });
  }

  openUploadModal(modal: TemplateRef<any>) {
    this.selectedUploadDocument = null;
    this.dialogRef = this.dialog.open(modal);
  }

  closeUploadModal() {
    if (this.dialogRef) {
      this.dialogRef.close();
      this.dialogRef = null;
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    this.selectedUploadDocument = file ? file : null;
  }

  confirmUpload() {
    if (!this.selectedUploadDocument) return;
    this.loading = true;
    this.documentService.uploadDocument(this.selectedUploadDocument).subscribe({
      next: () => {
        this.closeUploadModal();
        this.loadDocuments(this.page);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  consultDocuments() {
    this.loadDocuments(0);
    this.selectedDocument = null;
    this.selectedDocumentChunks = [];
  }

  onPageChange(event: any) {
    this.loadDocuments(event.pageIndex);
  }

  deleteFile(file: Document) {
    this.documentToDelete = file;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.documentToDelete) {
      this.loading = true;
      this.documentService.deleteDocument(this.documentToDelete.id).subscribe({
        next: () => {
          this.documents = this.documents.filter(doc => doc.id !== this.documentToDelete?.id);
          this.documentToDelete = null;
          this.showDeleteModal = false;
          this.loading = false;
        },
        error: () => {
          // Manejo de error (puedes mostrar un mensaje)
          this.showDeleteModal = false;
          this.loading = false;
        }
      });
    }
  }

  cancelDelete() {
    this.documentToDelete = null;
    this.showDeleteModal = false;
  }

  // Backwards compatibility for template
  get files() { return this.documents; }
  get selectedFile() { return this.selectedDocument; }
  get selectedFileChunks() { return this.selectedDocumentChunks; }
  get selectedUploadFile() { return this.selectedUploadDocument; }

  selectFile(file: Document) { this.selectDocument(file); }
  consultFiles() { this.consultDocuments(); }
}

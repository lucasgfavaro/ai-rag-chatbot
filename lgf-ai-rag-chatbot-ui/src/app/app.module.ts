import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';

import { DocumentUploadComponent } from './document-upload/document-upload.component';
import { ChatbotComponent } from './chatbot/chatbot.component';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    CommonModule,
    DocumentUploadComponent,
    ChatbotComponent
  ],
})
export class AppModule {}

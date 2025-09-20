import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { CommonModule, DatePipe, JsonPipe } from '@angular/common';

// Angular Material modules - usando las rutas correctas
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';

import { ChatbotComponent } from './chatbot/chatbot.component';

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpClientModule,
    CommonModule,
    // Angular Material modules
    MatButtonToggleModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatToolbarModule,
    MatButtonModule,
    MatListModule,
    MatPaginatorModule,
    MatCardModule,
    // Components
    ChatbotComponent
  ],
  providers: [DatePipe, JsonPipe],
})
export class AppModule {}

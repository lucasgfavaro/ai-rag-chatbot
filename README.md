# AI RAG Chatbot 🤖

A complete chatbot system with Retrieval-Augmented Generation (RAG) that allows intelligent queries on uploaded documents.

## 📋 Project Description

This project implements an AI chatbot that uses RAG (Retrieval-Augmented Generation) techniques to answer questions based on previously uploaded documents. The system consists of a Java backend with Spring Boot and a modern Angular frontend with Material Design.

## 🏗️ System Architecture

### Backend (Java + Spring Boot)
- **Location**: `lgf-ai-rag-chatbot-api/`
- **Technologies**: Java, Spring Boot, Maven, MongoDB, Pinecone, OpenAI
- **Features**:
  - REST API for the chatbot
  - Document processing and storage with MongoDB
  - RAG (Retrieval-Augmented Generation) implementation
  - Vector embeddings management with Pinecone
  - AI model integration with OpenAI GPT
  - Semantic search and document retrieval

### Frontend (Angular + Material Design)
- **Location**: `lgf-ai-rag-chatbot-ui/`
- **Technologies**: Angular, TypeScript, Angular Material, CSS3
- **Features**:
  - Modern chat interface with message bubbles
  - Document upload
  - Conversation history
  - Responsive design
  - Material Design components

## ✨ Main Features

### 🤖 Intelligent Chatbot
- Modern chat interface with custom avatars
- Complete conversation history
- Real-time loading indicators
- Differentiated message bubbles (user vs. bot)
- Informative error messages

### 📄 Document Management
- Document upload system
- Content processing and vectorization
- Semantic search in documents

### 🎨 Modern Interface
- Material Design
- Smooth gradients and animations
- Responsive design for mobile and desktop
- Elegant blue-purple color theme

## 🚀 Installation and Configuration

### Prerequisites
- Java 11 or higher
- Node.js 16 or higher
- Maven 3.6 or higher
- Angular CLI
- MongoDB instance
- Pinecone API key
- OpenAI API key

### Backend (Spring Boot)
```bash
cd lgf-ai-rag-chatbot-api
mvn clean install
mvn spring-boot:run
```

### Frontend (Angular)
```bash
cd lgf-ai-rag-chatbot-ui
npm install
npm start
```

The frontend will be available at `http://localhost:4200` and the backend at `http://localhost:8080`.

## 🛠️ Technologies Used

### Backend
- **Java**: Main programming language
- **Spring Boot**: Framework for REST APIs
- **Maven**: Dependency management
- **MongoDB**: Document database for storing uploaded files and metadata
- **Pinecone**: Vector database for embeddings and semantic search
- **OpenAI**: AI model for natural language processing and generation
- **Spring Web**: For HTTP endpoints
- **Spring Data MongoDB**: Data persistence layer

### Frontend
- **Angular**: Frontend framework
- **TypeScript**: Typed language
- **Angular Material**: UI components
- **RxJS**: Reactive programming
- **CSS3**: Custom styles with gradients and animations

## 📊 RAG System Features

### Retrieval
- Document vectorization using OpenAI embeddings
- Vector storage and indexing with Pinecone
- Semantic search in uploaded documents
- Fragment relevance ranking
- Context retrieval for AI responses

### Augmented Generation
- Contextualized response generation using OpenAI GPT
- Use of retrieved information from documents
- Coherent and accurate responses based on uploaded content
- Real-time AI processing

## 🎯 Use Cases

1. **Queries on corporate documents**
2. **Internal knowledge assistant**
3. **Document-based Q&A system**
4. **Educational chatbot based on specific materials**

## 🔧 Development Configuration

### Environment Variables
Configure the following variables in your application.properties or environment:
```properties
# OpenAI Configuration
openai.api.key=your-openai-api-key
openai.model=gpt-3.5-turbo

# Pinecone Configuration
pinecone.api.key=your-pinecone-api-key
pinecone.environment=your-pinecone-environment
pinecone.index.name=your-index-name

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/ai-rag-chatbot
```

### Project Structure
```
ai-rag-chatbot/
├── lgf-ai-rag-chatbot-api/      # Backend API (Java/Spring Boot)
│   ├── src/main/java/           # Java source code
│   ├── src/main/resources/      # Configurations
│   └── pom.xml                  # Maven dependencies
├── lgf-ai-rag-chatbot-ui/       # Frontend (Angular)
│   ├── src/app/                 # Angular components
│   ├── src/assets/              # Static resources
│   └── package.json             # NPM dependencies
├── .gitignore                   # Git ignore rules
└── README.md                    # This file
```

## 🚦 API Endpoints

### Chatbot
- `POST /api/chatbot/ask` - Send question to chatbot
- `GET /api/chatbot/history` - Get conversation history

### Documents
- `POST /api/documents/upload` - Upload new document
- `GET /api/documents` - List uploaded documents
- `DELETE /api/documents/{id}` - Delete document

## 🎨 UI Features

### Main Components
- **ChatbotComponent**: Main chat interface
- **DocumentUploadComponent**: Document upload
- **Material Design**: Buttons, input fields, spinners


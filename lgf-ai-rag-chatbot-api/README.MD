# AI RAG Chatbot API

## Context Management

To maintain context, first perform a similarity search, then retrieve the previous and next chunks around the match to preserve chat context.

When chunking chat content, stay within the 6000 token limit and chunk accordingly.

When searching for similarities, set a limit and investigate how long the prompt can be.

## Swagger & OpenAPI

After restoring the context-path, Swagger works correctly. Once you restart your application, you can access your API documentation at:

- Swagger UI: [http://localhost:8080/rag-chatbot/swagger-ui/index.html](http://localhost:8080/rag-chatbot/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/rag-chatbot/v3/api-docs](http://localhost:8080/rag-chatbot/v3/api-docs)

---

Feel free to update this README with more details about endpoints, usage, and architecture as your project evolves.

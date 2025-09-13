
Lo que deberia hacer para mantener contexto es primero hacer el similarity search y luego ir a buscar los chunks anteriores y posteriores a la coincidencia para mantener el contexto del chat. 
Al haer el chunk del chat deberia mantenerme dentro del limite de los 6000 tokens e ir chunckeado por eso. 

Al ir a buscar al similitudes dberia establecer un limite y tengo que investigar cuando largo puede ser el prompt


Perfecto, restablecí el context-path. Ahora que Swagger funciona correctamente, después de reiniciar tu aplicación podrás acceder a la documentación de tu API en:
http://localhost:8080/rag-chatbot/swagger-ui/index.html
Y el endpoint de OpenAPI JSON estará en:
http://localhost:8080/rag-chatbot/v3/api-docs
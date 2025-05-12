### 1. Project Description
`This is the backend for the Smart PR Reviewer project. It provides REST APIs for AI-powered code review.`

### 2. Ollama (LLM) Setup
**Add after Prerequisites:**

## Ollama (LLM) Setup

1. Download and install Ollama from [https://ollama.com/](https://ollama.com/)

2. Start the Ollama server:

   ```ollama serve```


3. Pull the required model (e.g., CodeLlama):
 
   ```ollama pull codellama```



### 3. GitHub App Setup
**Replace/expand the current instructions:**
```markdown
## GitHub App Setup

1. Go to GitHub → Settings → Developer settings → GitHub Apps → New GitHub App.
2. Set permissions for:
   - Pull requests: Read
   - Contents: Read
   - Metadata: Read
3. Generate and download a private key.
4. Note your App ID and Webhook Secret.
5. Install the app on your repository.
```

### 4. Environment Variables
**Clarify how to set secrets:**

## Configuration

Set the following environment variables (or add them to your `application.yml`):

```bash
export GITHUB_APP_ID=your-app-id
export GITHUB_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----"
export GITHUB_WEBHOOK_SECRET=your-webhook-secret
```


### 5. Running the Backend
**Clarify the order:**

## Running the Backend

1. Ensure Ollama is running and the model is pulled.
2. Start the backend:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```


### 6. Usage
**Clarify manual PR review:**

## Usage

- Open the frontend and paste your PR details to trigger a review.
- The backend will fetch PR details from GitHub and use Ollama to generate review comments.

# AI Code Review Assistant

An AI-powered code review assistant that automatically reviews pull requests for logic correctness, security vulnerabilities, performance bottlenecks, and best practices.

## Features

- Integrates with GitHub/GitLab/Bitbucket
- Auto-reviews pull requests for:
  - Logic correctness
  - Security vulnerabilities
  - Performance bottlenecks
  - Style and best practices
- Understands project-specific context
- Leaves actionable comments directly on PR lines
- Suggests:
  - Missing unit/integration tests
  - Documentation updates
  - Refactoring opportunities

## Tech Stack

- Backend: Spring Boot (Java)
- Git Integration: GitHub Webhooks & GitHub REST API
- AI Engine: OpenAI API
- Project Context: YAML/JSON configuration

## Prerequisites

- Java 17 or higher
- Maven
- OpenAI API key
- GitHub App credentials

## Configuration

1. Create a GitHub App and get the following credentials:
   - App ID
   - Private Key
   - Webhook Secret

2. Set the following environment variables:
   ```bash
   export GITHUB_APP_ID=your-app-id
   export GITHUB_PRIVATE_KEY=your-private-key
   export GITHUB_WEBHOOK_SECRET=your-webhook-secret
   export OPENAI_API_KEY=your-openai-api-key
   ```

3. Configure the application.yml file with your settings.

## Building and Running

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/ai-code-review.git
   cd ai-code-review
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Usage

1. Install the GitHub App on your repository
2. Configure the webhook URL in your GitHub App settings
3. Create a pull request
4. The AI will automatically review the changes and post comments

## Project Structure

```
src/main/java/com/aicodereview/
├── controller/
│   └── WebhookController.java
├── model/
│   ├── CodeReview.java
│   ├── ReviewComment.java
│   ├── ReviewStatus.java
│   └── CommentType.java
├── repository/
│   └── CodeReviewRepository.java
├── service/
│   ├── AIReviewService.java
│   ├── CodeReviewService.java
│   ├── GitHubService.java
│   └── impl/
│       ├── AIReviewServiceImpl.java
│       ├── CodeReviewServiceImpl.java
│       └── GitHubServiceImpl.java
└── AICodeReviewApplication.java
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 
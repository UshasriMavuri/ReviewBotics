# ReviewBotics Frontend

A modern web application for AI-powered code review assistance. This frontend application provides a user-friendly interface for managing code reviews, repository integrations, and review settings.

## Features

- 🎯 AI-powered code review suggestions
- 🔄 GitHub/GitLab/Bitbucket integration
- 📊 Review dashboard with statistics
- 🔍 Advanced search and filtering
- ⚙️ Customizable review settings
- 🔔 Notification preferences

## Tech Stack

- React 18
- TypeScript
- Tailwind CSS
- Material-UI
- Framer Motion
- React Router

## Getting Started

### Prerequisites

- Node.js 16.x or later
- npm 7.x or later

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/reviewbotics.git
cd reviewbotics/ai-code-review-frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

The application will be available at `http://localhost:3000`.

### Building for Production

To create a production build:

```bash
npm run build
```

The build artifacts will be stored in the `build/` directory.

## Development

### Project Structure

```
src/
  ├── components/     # Reusable UI components
  ├── pages/         # Page components
  ├── hooks/         # Custom React hooks
  ├── services/      # API services
  ├── types/         # TypeScript type definitions
  ├── utils/         # Utility functions
  └── App.tsx        # Main application component
```

### Available Scripts

- `npm start` - Runs the app in development mode
- `npm test` - Launches the test runner
- `npm run build` - Builds the app for production
- `npm run eject` - Ejects from Create React App

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

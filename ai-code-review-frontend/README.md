# AI Code Review Frontend

A modern, user-friendly React application for AI-powered code review analysis with seamless GitHub integration.

## 🎨 UI/UX Features

### User Interface
- Clean, minimalist design with Material-UI components
- Responsive layout that works on desktop and mobile
- Dark/Light theme support
- Intuitive navigation with breadcrumbs
- Real-time feedback and loading states
- Error handling with user-friendly messages

### User Experience
- Single input field for PR URL for quick access
- Progressive loading of review results
- Interactive review comments with code snippets
- Collapsible sections for better content organization
- Keyboard shortcuts for common actions
- Toast notifications for important updates

## 🏗️ UI Architecture

```
src/
├── components/
│   └── Layout.tsx           # Main layout component
├── pages/
│   ├── CodeReview.tsx       # Main code review page
│   └── Home.tsx             # Landing/home page
├── services/
│   └── api.ts               # API service functions
├── theme/
│   └── index.ts             # Theme configuration
├── App.css                  # App-wide styles
├── App.tsx                  # App root component
├── index.css                # Global styles
├── index.tsx                # App entry point
├── logo.svg                 # App logo
├── react-app-env.d.ts       # React environment types
├── reportWebVitals.ts       # Web vitals reporting
└── setupTests.ts            # Test setup
```

## 🔄 Integration Features

### GitHub Integration
- Direct PR URL input support
- Real-time PR status updates
- Automatic PR metadata fetching
- Support for GitHub authentication
- Webhook integration for updates

### API Integration
- RESTful API communication
- Error handling and retry mechanisms

## 🚀 Getting Started

1. **Install dependencies:**
```bash
npm install
```

2. **Configure environment variables:**
Create a `.env` file in the root directory and add:
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_GITHUB_CLIENT_ID=your_github_client_id
```

3. **Start the development server:**
```bash
npm start
```

## 🤝 Contributing

1. Create a new branch for your feature:
```bash
git checkout -b feature/your-feature-name
```

2. Make your changes and commit them:
```bash
git add .
git commit -m "feat: add new feature"
```

3. Push your branch and create a pull request:
```bash
git push origin feature/your-feature-name
```

## 📦 Available Scripts

- `npm start`: Start development server
- `npm test`: Run tests
- `npm run build`: Create production build
- `npm run lint`: Run ESLint
- `npm run format`: Format code with Prettier

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

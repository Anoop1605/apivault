# API Vault Frontend

Modern React + TypeScript frontend for API Vault microservices.

## Project Structure

```
frontend/
├── public/              # Static assets
├── src/
│   ├── components/      # Reusable React components
│   ├── pages/           # Page components
│   ├── services/        # API service calls
│   ├── App.tsx          # Main App component
│   ├── main.tsx         # Entry point
│   └── index.css        # Global styles
├── package.json         # Dependencies
├── tsconfig.json        # TypeScript configuration
└── vite.config.ts       # Vite configuration
```

## Getting Started

### Prerequisites
- Node.js 16+ and npm/yarn

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
npm run dev
```

The app will be available at `http://localhost:3000`

### Build for Production

```bash
npm run build
```

### Environment Variables

Create a `.env` file based on `.env.example`:

```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=API Vault
```

## API Integration

All API calls should go through the `src/services/api.ts` service layer which handles:
- Base URL configuration
- Default headers
- Request/response interceptors
- Error handling

Example:
```typescript
import { apiService } from './services/api'

const response = await apiService.getHealth()
```

## Build & Deployment

The Vite build creates optimized production bundles in the `dist` directory.

Configure your backend proxy in `vite.config.ts` for API calls during development.

## Technology Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router** - Routing
- **Axios** - HTTP client
- **ESLint** - Code quality


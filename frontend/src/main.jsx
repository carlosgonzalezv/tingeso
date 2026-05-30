import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ReactKeycloakProvider } from '@react-keycloak/web';
import keycloak from './services/Keycloak.js';
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <ReactKeycloakProvider
            authClient={keycloak}
            initOptions={{
                onLoad: 'login-required',
                checkLoginIframe: false
            }}
        >
            <App />
        </ReactKeycloakProvider>
    </StrictMode>,
)

declare const AURE_API_URL: string | undefined;

export const API_BASE_URL = typeof AURE_API_URL === 'undefined' ? 'http://localhost:8080' : AURE_API_URL;

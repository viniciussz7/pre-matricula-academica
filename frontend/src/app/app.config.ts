import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { routes } from './app.routes';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { provideIcons } from '@ng-icons/core';
import {
  heroUserGroup, heroClock, heroBookOpen, heroCalendarDays,
  heroAcademicCap, heroClipboardDocumentCheck, heroChartBar
} from '@ng-icons/heroicons/outline';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // Habilita o HttpClient e avisa que vamos usar Interceptors via Injeção de Dependência
    provideHttpClient(withInterceptorsFromDi()),
    provideIcons({
      heroUserGroup, heroClock, heroBookOpen, heroCalendarDays,
      heroAcademicCap, heroClipboardDocumentCheck, heroChartBar
    }),
    // Registra o seu Interceptor para colar o JWT em todas as chamadas
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }

  ]
};
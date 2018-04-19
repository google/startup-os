export * from './app.component';
export * from './pageNotFoundComponent';
export * from './login/login.component';
export * from './dashboard';
export * from './shared/services';

import { LoginComponent } from './login/login.component';
import { PageNotFoundComponent } from './pageNotFoundComponent';

export const AppComponents = [LoginComponent, PageNotFoundComponent];

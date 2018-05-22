import { DashboardModule } from '@/dashboard/dashboard.module';
import { AuthGuard } from '@/shared/services/auth.guard';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { canActivate: [AuthGuard], path: '', loadChildren: () => DashboardModule }
];

export const LayoutRoutes = RouterModule.forChild(routes);

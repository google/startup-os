import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { NotificationService } from './notification.service';

@Injectable()
export class ExceptionService {
  constructor(
    private router: Router,
    private notificationService: NotificationService,
  ) { }

  // Diff not found.
  // Show error message and open root
  diffNotFound(): void {
    this.notificationService.error('Diff not found');
    this.router.navigate(['/']);
  }

  // File not found.
  // Show error message and open diff
  fileNotFound(diffId: number): void {
    this.notificationService.error('File not found');
    this.router.navigate(['/diff/' + diffId]);
  }
}

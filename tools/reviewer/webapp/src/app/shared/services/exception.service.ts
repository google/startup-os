import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { NotificationService } from './notification.service';

@Injectable()
export class ExceptionService {
  constructor(
    private router: Router,
    private notificationService: NotificationService,
  ) { }

  diffNotFound(): void {
    // Diff not found.
    // Show error message and open root
    this.notificationService.error('Diff not found');
    this.router.navigate(['/']);
  }

  fileNotFound(diffId: number): void {
    // File not found.
    // Show error message and open diff
    this.notificationService.error('File not found');
    this.router.navigate(['/diff/' + diffId]);
  }
}

export * from './auth.service';
export * from './difference.service';
export * from './firebase.service';
export * from './notification.service';
export * from './highlight.service';
export * from './selection.service';
export * from './encoding.service';
export * from './review.service';

import { AuthGuard } from '@/shared/services/auth.guard';
import { AuthService } from './auth.service';
import { DifferenceService } from './difference.service';
import { EncodingService } from './encoding.service';
import { FirebaseService } from './firebase.service';
import { HighlightService } from './highlight.service';
import { NotificationService } from './notification.service';
import { ReviewService } from './review.service';
import { SelectionService } from './selection.service';

export const Services = [
  AuthGuard,
  AuthService,
  DifferenceService,
  FirebaseService,
  HighlightService,
  NotificationService,
  SelectionService,
  ReviewService,
  EncodingService,
];

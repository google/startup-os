export * from './auth.service';
export * from './difference.service';
export * from './firebase.service';
export * from './notification.service';
export * from './highlight.service';
export * from './selection.service';
export * from './encoding.service';
export * from './localserver.service';

import { AuthGuard } from '@/shared/services/auth.guard';
import { AuthService } from './auth.service';
import { DifferenceService } from './difference.service';
import { EncodingService } from './encoding.service';
import { FirebaseService } from './firebase.service';
import { HighlightService } from './highlight.service';
import { LocalserverService } from './localserver.service';
import { NotificationService } from './notification.service';
import { SelectionService } from './selection.service';

export const Services = [
  AuthGuard,
  AuthService,
  DifferenceService,
  FirebaseService,
  HighlightService,
  NotificationService,
  SelectionService,
  EncodingService,
  LocalserverService,
];

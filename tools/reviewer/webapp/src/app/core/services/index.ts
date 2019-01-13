export * from './auth.guard';
export * from './auth.service';
export * from './firebase.service';
export * from './firebase-state.service';
export * from './notification.service';
export * from './highlight.service';
export * from './encoding.service';
export * from './localserver.service';
export * from './exception.service';
export * from './diff-update.service';
export * from './user.service';
export * from './document-event.service';
export * from './text-diff.service';
export * from './select-dashboard.service';

// Services
import { AuthGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { DiffUpdateService } from './diff-update.service';
import { DocumentEventService } from './document-event.service';
import { EncodingService } from './encoding.service';
import { ExceptionService } from './exception.service';
import { FirebaseStateService } from './firebase-state.service';
import { FirebaseService } from './firebase.service';
import { HighlightService } from './highlight.service';
import { LocalserverService } from './localserver.service';
import { NotificationService } from './notification.service';
import { SelectDashboardService } from './select-dashboard.service';
import { TextDiffService } from './text-diff.service';
import { UserService } from './user.service';
export const ServiceList = [
  AuthGuard,
  AuthService,
  FirebaseService,
  FirebaseStateService,
  HighlightService,
  NotificationService,
  EncodingService,
  LocalserverService,
  ExceptionService,
  DiffUpdateService,
  UserService,
  SelectDashboardService,
  DocumentEventService,
  TextDiffService,
];

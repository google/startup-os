import { AuthGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { EncodingService } from './encoding.service';
import { FirebaseService } from './firebase.service';
import { HighlightService } from './highlight.service';
import { LocalserverService } from './localserver.service';
import { NotificationService } from './notification.service';

export const ServiceList = [
  AuthGuard,
  AuthService,
  FirebaseService,
  HighlightService,
  NotificationService,
  EncodingService,
  LocalserverService,
];

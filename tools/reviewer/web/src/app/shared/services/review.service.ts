import { Injectable } from '@angular/core';

import { Diff, Reviewer } from '@/shared';
import { AuthService } from './auth.service';

@Injectable()
export class ReviewService {

  constructor(private authService: AuthService) {}

  getReviewerWithTheUsername(diff: Diff, username: string): Reviewer {
    for (const reviewer of diff.getReviewerList()) {
      const reviewerUsername = this.authService
        .getUsername(reviewer.getEmail());

      if (reviewerUsername === username) {
        return reviewer;
      }
    }
  }
}

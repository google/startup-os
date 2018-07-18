import { Injectable } from '@angular/core';

import { Diff, Reviewer, AuthService } from '@/shared';

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

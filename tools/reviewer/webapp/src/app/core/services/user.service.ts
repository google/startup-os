import { Injectable } from '@angular/core';

import { Author, Diff, File, Reviewer } from '@/core/proto';

@Injectable()
export class UserService {
  email: string;

  // bob@mail.com -> bob
  getUsername(email: string): string {
    return email.split('@')[0];
  }

  // Request attention of the user, if it's not requested,
  // or cancel the attention, if it's already requested.
  changeAttention(user: Reviewer | Author): void {
    user.setNeedsAttention(!user.getNeedsAttention());
  }

  // Remove reviewer with the email from reviewer list
  removeFromReviewerList(diff: Diff, email: string): void {
    const reviewerList: Reviewer[] = diff.getReviewerList().filter(
      reviewer => reviewer.getEmail() !== email,
    );
    diff.setReviewerList(reviewerList);
  }

  // Remove email from CC list
  removeFromCcList(diff: Diff, email: string): void {
    const ccList: string[] = diff.getCcList().filter(cc => cc !== email);
    diff.setCcList(ccList);
  }

  // Get reviewer from reviewer list by email
  getReviewer(diff: Diff, email: string): Reviewer {
    for (const reviewer of diff.getReviewerList()) {
      if (reviewer.getEmail() === email) {
        return reviewer;
      }
    }
  }

  isFileReviewed(reviewer: Reviewer, file: File): boolean {
    for (const reviewedFile of reviewer.getReviewedList()) {
      if (
        reviewedFile.getFilenameWithRepo() === file.getFilenameWithRepo() &&
        reviewedFile.getCommitId() === file.getCommitId()
      ) {
        return true;
      }
    }
  }
}

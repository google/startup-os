import { Diff } from '@/core/proto';

export interface Status {
  label: string;
  icon: string;
}

export const statusList: Status[] = [];

statusList[Diff.Status.REVIEW_NOT_STARTED] = {
  label: 'Review Not Started',
  icon: 'ic-review-not-started.svg',
};
statusList[Diff.Status.NEEDS_MORE_WORK] = {
  label: 'Needs More Work',
  icon: 'ic-needs-more-work.svg',
};
statusList[Diff.Status.UNDER_REVIEW] = {
  label: 'Under Review',
  icon: 'ic-under-review.svg',
};
statusList[Diff.Status.ACCEPTED] = {
  label: 'Accepted',
  icon: 'ic-accepted.svg',
};
statusList[Diff.Status.SUBMITTING] = {
  label: 'Submitting',
  icon: 'ic-submitting.svg',
};
statusList[Diff.Status.SUBMITTED] = {
  label: 'Submitted',
  icon: 'ic-submitted.svg',
};
statusList[Diff.Status.REVERTING] = {
  label: 'Reverting',
  icon: 'ic-reverting.svg',
};
statusList[Diff.Status.REVERTED] = {
  label: 'Reverted',
  icon: 'ic-reverted.svg',
};

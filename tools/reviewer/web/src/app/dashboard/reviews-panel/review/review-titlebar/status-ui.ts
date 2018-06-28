import { Diff } from '@/shared/shell';

interface StatusUI {
  label: string;
  icon: string;
  spanClass?: string;
  iconClass: string;
}

export const statusList: StatusUI[] = [];

statusList[Diff.Status.REVIEW_NOT_STARTED] = {
  label: 'Review Not Started',
  icon: 'trip_origin',
  iconClass: 'reviewNotStarted',
};
statusList[Diff.Status.NEEDS_MORE_WORK] = {
  label: 'Needs More Work',
  icon: 'build',
  spanClass: 'needsMoreWork',
  iconClass: 'smaller',
};
statusList[Diff.Status.UNDER_REVIEW] = {
  label: 'Under Review',
  icon: 'message',
  spanClass: 'underReview',
  iconClass: 'smaller-icon',
};
statusList[Diff.Status.ACCEPTED] = {
  label: 'Accepted',
  icon: 'thumb_up',
  spanClass: 'accepted',
  iconClass: 'smaller-icon',
};
statusList[Diff.Status.SUBMITTING] = {
  label: 'Submitting',
  icon: 'check_circle',
  iconClass: 'submitted',
};
statusList[Diff.Status.SUBMITTED] = {
  label: 'Submitted',
  icon: 'check_circle',
  iconClass: 'submitted',
};
statusList[Diff.Status.REVERTING] = {
  label: 'Reverting',
  icon: 'cancel',
  iconClass: 'reverting',
};
statusList[Diff.Status.REVERTED] = {
  label: 'Reverted',
  icon: 'cancel',
  iconClass: 'reverted',
};

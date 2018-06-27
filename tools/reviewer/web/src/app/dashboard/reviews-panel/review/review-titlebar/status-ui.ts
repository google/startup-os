import { Diff } from '@/shared/shell';

interface StatusUI {
  label: string;
  icon: string;
}

export const statusList: StatusUI[] = [];

statusList[Diff.Status.REVIEW_NOT_STARTED] = {
  label: 'Review Not Started',
  icon: 'trip_origin'
};
statusList[Diff.Status.NEEDS_MORE_WORK] = {
  label: 'Needs More Work',
  icon: 'build'
};
statusList[Diff.Status.UNDER_REVIEW] = {
  label: 'Under Review',
  icon: 'message'
};
statusList[Diff.Status.ACCEPTED] = {
  label: 'Accepted',
  icon: 'thumb_up'
};
statusList[Diff.Status.SUBMITTING] = {
  label: 'Submitting',
  icon: 'check_circle'
};
statusList[Diff.Status.SUBMITTED] = {
  label: 'Submitted',
  icon: 'check_circle'
};
statusList[Diff.Status.REVERTING] = {
  label: 'Reverting',
  icon: 'cancel'
};
statusList[Diff.Status.REVERTED] = {
  label: 'Reverted',
  icon: 'cancel'
};

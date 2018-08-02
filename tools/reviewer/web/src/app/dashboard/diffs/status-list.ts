import { Diff } from '@/shared/shell';

export interface Status {
  label: string;
  icon: string;
  color: string;
}

export const statusList: Status[] = [];

statusList[Diff.Status.REVIEW_NOT_STARTED] = {
  label: 'Review Not Started',
  icon: 'trip_origin',
  color: '#1545bd',
};
statusList[Diff.Status.NEEDS_MORE_WORK] = {
  label: 'Needs More Work',
  icon: 'build',
  color: '#1545bd',
};
statusList[Diff.Status.UNDER_REVIEW] = {
  label: 'Under Review',
  icon: 'message',
  color: '#eac92c',
};
statusList[Diff.Status.ACCEPTED] = {
  label: 'Accepted',
  icon: 'thumb_up',
  color: '#12a736',
};
statusList[Diff.Status.SUBMITTING] = {
  label: 'Submitting',
  icon: 'check_circle',
  color: '#12a736',
};
statusList[Diff.Status.SUBMITTED] = {
  label: 'Submitted',
  icon: 'check_circle',
  color: '#12a736',
};
statusList[Diff.Status.REVERTING] = {
  label: 'Reverting',
  icon: 'cancel',
  color: '#db4040',
};
statusList[Diff.Status.REVERTED] = {
  label: 'Reverted',
  icon: 'cancel',
  color: '#db4040',
};

export enum Status {
  REVIEW_NOT_STARTED = 0,
  NEEDS_MORE_WORK = 1,
  UNDER_REVIEW = 2,
  ACCEPTED = 3,
  SUBMITTED = 4,
  REVERTED = 5
}

export interface Diff {
  number: number;
  author: string;
  reviewers: string[];
  description: string;
  bug: string;
  status: Status;
  workspace: string;
  createdTimestamp: number;
  modifiedTimestamp: number;
  files: File[];
  snapshots: Snapshot[];
  threads: Thread[];
  pullRequestId: string;
  needAttentionOf: string[];
  cc: string[];
}

interface Files {
  file: File[];
}

enum FileAction {
  ADD = 0,
  DELETE = 1,
  RENAME = 2,
  MODIFY = 3
}

interface File {
  filePosition: string;
  fileAction: FileAction;
}

export interface Snapshot {
  timestamp: number;
  gitCommit: string;
  forReview: boolean;
  files: string[];
}

export interface Thread {
  snapshot: number;
  filename: string;
  lineNumber: number;
  isDone: boolean;
  comments: Comment[];
}

export interface Comment {
  content: string;
  timestamp: number;
  createdBy: string;
}

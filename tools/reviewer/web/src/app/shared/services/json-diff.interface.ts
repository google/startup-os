// TODO: delete the file, when binary from firebase will be available.
// We still need this to support json data from firebase.

enum Status {
  REVIEW_NOT_STARTED = 0,
  NEEDS_MORE_WORK = 1,
  UNDER_REVIEW = 2,
  ACCEPTED = 3,
  SUBMITTED = 4,
  REVERTED = 5
}

export interface JsonDiff {
  number: number;
  author: string;
  reviewers: string[];
  description: string;
  bug: string;
  status: Status;
  workspace: string;
  createdTimestamp: number;
  modifiedTimestamp: number;
  files: JsonFile[];
  snapshots: JsonSnapshot[];
  threads: JsonThread[];
  pullRequestId: string;
  needAttentionOf: string[];
  cc: string[];
}

enum FileAction {
  ADD = 0,
  DELETE = 1,
  RENAME = 2,
  MODIFY = 3
}

export interface JsonFile {
  filePosition: string;
  fileAction: FileAction;
}

export interface JsonSnapshot {
  timestamp: number;
  gitCommit: string;
  forReview: boolean;
  files: string[];
}

export interface JsonThread {
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

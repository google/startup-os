import {
  Comment,
  Diff,
  Difference,
  DifferenceService,
  FirebaseService,
  Line,
  ProtoService,
  Thread
} from '@/shared';
import { AuthService, NotificationService } from '@/shared/services';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  Params,
  Router
} from '@angular/router';

@Component({
  selector: 'app-diff',
  templateUrl: './diff.component.html',
  styleUrls: ['./diff.component.scss']
})
export class DiffComponent implements OnInit {
  file1Contents: Array<string>;
  file2Contents: Array<string>;

  // A collection of line-containers; each line-container
  // has an array of objects explaining how each code line was
  // made from another by insertion and removal operations.
  differences: Array<Array<Difference>> = [];

  // Number of panels of code on the front-end
  snapshots = ['leftSnapshot', 'rightSnapshot'];

  // Variable for setting width of each panel
  // default width for two panels
  flexWidth = '1 1 48%';

  // FilePath of current Diff
  filePath: string;

  // Current Diff
  diff: Diff;

  // Code snapshot Numbers
  leftSnapshot: number;
  rightSnapshot: number;

  // Threads for leftSnapshot will be added to ls and
  // threads for rightSnapshot will be added to rs
  threads: { ls: Array<Thread>; rs: Array<Thread> } = { ls: [], rs: [] };
  user: string;

  constructor(
    private activatedRoute: ActivatedRoute,
    private auth: AuthService,
    private cdr: ChangeDetectorRef,
    private diffService: DifferenceService,
    private firebaseService: FirebaseService,
    private protoService: ProtoService,
    private router: Router,
    private notify: NotificationService
  ) {}

  ngOnInit() {
    // Get parameters from url
    const urlSnapshot = this.activatedRoute.snapshot;
    this.leftSnapshot = parseInt(urlSnapshot.queryParams['ls'], 10);
    this.rightSnapshot = parseInt(urlSnapshot.queryParams['rs'], 10);
    const diffId: string = urlSnapshot.url[0].path;
    this.filePath = urlSnapshot.url
      .splice(1)
      .map(v => v.path)
      .join('/');

    // Get a single review using diffId
    this.firebaseService.getDiff(diffId).subscribe(res => {
      // Create Diff from proto
      this.protoService.open.subscribe(error => {
        if (error) {
          throw error;
        }
        const review = res;
        this.diff = this.protoService.createDiff(review);
        this.diff.number = parseInt(diffId, 10);

        // Add threads for current file threads object
        this.processComments();
      });
    });

    // Get current user
    this.auth.getUser().subscribe(user => {
      const email = user.email;
      this.user = email.split('@')[0];
    });

    // Iterate over each file and load
    // it's contents
    this.files.forEach((v, i) => {
      this.loadVariable(v);
    });

    // Setting flexWidth based on number of panels/snapshots
    this.flexWidth = '1 1 ' + (100 / this.snapshots.length - 2) + '%';
  }

  /**
   * Get comment threads related to current snapshots
   * and current file
   */
  processComments(): void {
    this.threads.ls = this.diff.threads.filter(
      v => v.filename === this.filePath && v.snapshot === this.leftSnapshot
    );

    this.threads.rs = this.diff.threads.filter(
      v => v.filename === this.filePath && v.snapshot === this.rightSnapshot
    );
  }

  /**
   * Checks if a thread exists on this lineNumber and
   * this snpashot, If a thread doesn't exist; add it
   */
  addNewThread(s: string, lineNumber: number): void {
    const snapshot =
      s === 'leftSnapshot' ? this.leftSnapshot : this.rightSnapshot;

    // If no thread exists on this snapshot and lineNumber
    // then add the Thread
    if (
      this.diff.threads.filter(
        v => v.snapshot === snapshot && v.lineNumber === lineNumber
      ).length === 0
    ) {
      const t: Thread = {
        snapshot: snapshot,
        lineNumber: lineNumber,
        filename: this.filePath,
        comments: [],
        isDone: false
      };
      this.diff.threads.push(t);
      this.processComments();
    }
  }

  /**
   * Removes thread if it has only one comment in it
   */
  removeThread(thread: Thread): void {
    // Find index of this particular thread in the Diff
    const threadIndex = this.diff.threads.findIndex(
      t =>
        t.snapshot === thread.snapshot &&
        t.lineNumber === thread.lineNumber &&
        t.filename === thread.filename &&
        t.comments.length === 0
    );
    // Remove the thread from Diff
    if (threadIndex > 0) {
      this.diff.threads.splice(threadIndex, 1);
      this.processComments();
    }
  }

  /**
   * Checks if a thread exists for that particular snapshot,
   * and that particular line number
   * returns the thread if it exists and
   * null if the thread does not exist
   */
  tryGetThread(snapshot: string, lineNumber: number): Thread {
    // Get threads based on which snapshot it is
    const threads =
      snapshot === 'leftSnapshot' ? this.threads.ls : this.threads.rs;

    // Get all the lineNumbers which contain threads in
    // the selected snapshot
    const lineNumbersForThreads = threads.map(v => v.lineNumber);

    // Return 0 if there is no thread and returns
    // the thread if it exists
    return lineNumbersForThreads.indexOf(lineNumber) > -1
      ? threads[lineNumbersForThreads.indexOf(lineNumber)]
      : null;
  }

  /**
   * Update the diff to firebase after removing empty threads
   */
  updateThread(): void {
    this.diff.threads = this.diff.threads.filter(v => v.comments.length > 0);
    this.firebaseService
      .updateDiff(this.diff)
      .then(res => {
        // TODO make separate service for notifications
        this.notify.success('Comment Saved');
      })
      .catch(err => {
        this.notify.error('Some Error Occured');
      });
  }

  // Depreciated code below this comment

  loadVariable(fileContents: string): void {
    // Add space that can be rendered in the HTML
    // and split the content into lines
    const lines = fileContents.replace(/ /g, '\xa0\xa0').split('\n');
    if (!this.file1Contents) {
      // Load up contents of file 1
      this.file1Contents = lines;
    } else {
      // Load up contents of file 2
      this.file2Contents = lines;
      // Compute the diff from file contents
      this.differences = this.diffService.computeDiff(
        this.file1Contents,
        this.file2Contents
      );
    }
  }

  // Temporary file until we start getting
  // file contents from the server
  files: Array<string> = [
    `from firebase import firebase
    from google.protobuf import json_format
    
    from proto import messages_pb2
    from proto.messages_pb2 import Diff
    from python import global_imports
    from review_server import local_firebase
    
    """A data store for getting protos from github."""
    class Datastore:
      def __init__(self, filename=None):
        if filename:
          self._firebase = local_firebase.FirebaseApplication(filename)
        else:
          user = global_imports.getUser()
          
      """Returns diff. If none is found, throws a ValueError exception."""
      def getDiff(self, diffnum):
        _diff = self._firebase.get('/diffs', str(diffnum))
        if not _diff:
          raise ValueError('No diff with id {}'.format(diffnum))
        diff_message = messages_pb2.Diff()
        json_format.ParseDict(_diff, diff_message)
        return diff_message
    
      """Saves diff using given diffnum."""
      def setDiff(self, diffnum, diff):
        diff_json = json_format.MessageToDict(diff)
        self._firebase.put('/diffs', diffnum, diff_json)
    
      """Returns all diffs."""
      def getAllDiffs(self):
        _diffs = self._firebase.get('/', 'diffs') or {}
        all_diffs = []
        for _id, _diff in _diffs.items():
          diff_message = messages_pb2.Diff()
          json_format.ParseDict(_diff, diff_message)
          diff_message.number = int(_id)
          all_diffs.append(diff_message)
        return all_diffs
    
      def __diff_as_dict_query(self, query: Diff):
        keys = query.DESCRIPTOR.fields_by_name.keys()
        dict_query = {}
        for key in keys:
          if getattr(query, key, None):
            dict_query[key] = getattr(query, key)
        return dict_query
    
      """Returns diffs by query. Query is a Diff and for
        all returned diffs will match this field's value. For example, for a
        query with author="bob", all diffs returned will have author="bob".
      """
      def getDiffsByQuery(self, query):
        all_diffs = self.getAllDiffs()
        query = self.__diff_as_dict_query(query)
        query_result = []
        for diff in all_diffs:
          incl = True
          for key, value in query.items():
            if getattr(diff, key) != value:
              incl = False
          if incl:
            query_result.append(diff)
        return query_result
    
      """Returns the next available diffnum. Every call increases the next
        available diffnum by 1. This method is synchronized globally so
        that 2 callers on 2 machines always get different numbers.
      """
      def getNextAvailableDiffnum(self):
        lid = self._firebase.get('/','diff_latest_id') or 1
        self._firebase.put('/', 'diff_latest_id', lid+1)
        return lid`,

    `from firebase import firebase
    from google.protobuf import json_format
    
    from donot import anything
    from proto.messages_pb2 import Diff
    from python import global_imports
    from review_server import local_firebase
    
    """A data store for getting protos from github."""
    class Datastore:
      def __init__(self, filename=None):
        if filename:
          self._firebase = local_firebase.FirebaseApplication(filename)
        else:
          user = global_imports.getUser()

      """Returns diff. If none is found, throws a ValueError exception."""
      def getDiff(self, diffnum):
        _diff = self._firebase.get('/diffs', str(diffnum))
        if not _diff:
          raise ValueError('No diff with id {}'.format(diffnum))
        diff_message = messages_pb2.Diff()
        json_format.ParseDict(_diff, diff_message)
        return diff_message
    
      """Saves diff using given diffnum."""
      def setDiff(self, diffnum, diff):
        diff_json = json_format.MessageToDict(diff)
        self._firebase.put('/diffs', diffnum, diff_json)
    
      """Returns all diffs."""
      def getAllDiffs(self):
        _diffs = self._firebase.get('/', 'diffs') or {}
        all_diffs = []
        for _id, _diff in _diffs.items():
          diff_message = messages_pb2.Diff()
          json_format.ParseDict(_diff, diff_message)
          diff_message.number = int(_id)
          all_diffs.append(diff_message)
        return all_diffs
    
      def __diff_as_dict_query(self, query: Diff):
        keys = query.DESCRIPTOR.fields_by_name.keys()
        dict_query = {}
        for key in keys:
          if getattr(query, key, None):
            dict_query[key] = getattr(query, key)
        return dict_query
    
      """Returns diffs by query. Query is a Diff and for
        all returned diffs will match this field's value. For example, for a
        query with author="bob", all diffs returned will have author="bob".
      """
      def getDiffsByQuery(self, query):
        all_diffs = self.getAllDiffs()
        query = self.__diff_as_dict_query(query)
        query_result = []
        for diff in all_diffs:
          incl = True
          for key, value in query.items():
            if getattr(diff, key) != value:
              incl = False
          if incl:
            query_result.append(diff)
        return query_result
    
      """Returns the next available diffnum. Every call increases the next
        available diffnum by 1. This method is synchronized globally so
        that 2 callers on 2 machines always get different numbers.
      """
      def getNextAvailableDiffnum(self):
        lid = self._firebase.get('/','diff_latest_id') or 1
        self._firebase.put('/', 'diff_latest_id', lid+1)
        return lid
    `
  ];
}

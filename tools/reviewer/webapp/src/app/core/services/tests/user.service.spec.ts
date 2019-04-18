import { Reviewer, File, Diff } from '@/core/proto';
import { UserService } from '../user.service';

describe('UserService', () => {
  let userService: UserService;
  beforeEach(() => {
    userService = new UserService();
  });

  it('should gets username', () => {
    let email: string;
    let username: string;

    email = 'easy@email.com';
    username = userService.getUsername(email);
    expect(username).toBe('easy');

    email = 'Complex-name.Tag_17@web-site.info';
    username = userService.getUsername(email);
    expect(username).toBe('Complex-name.Tag_17');

    email = 'username';
    username = userService.getUsername(email);
    expect(username).toBe('username');

    email = '';
    username = userService.getUsername(email);
    expect(username).toBeUndefined();

    email = undefined;
    username = userService.getUsername(email);
    expect(username).toBeUndefined();
  });

  it('should gets review status', () => {
    const reviewer = new Reviewer();
    const file1 = new File();
    const file2 = new File();

    file1.setFilenameWithRepo('path/to/file1');
    file1.setCommitId('abc');
    file2.setFilenameWithRepo('path/to/file2');
    file2.setCommitId('xyz');

    expect(userService.isFileReviewed(reviewer, file2)).toBeFalsy();

    reviewer.addReviewed(file1);
    expect(userService.isFileReviewed(reviewer, file2)).toBeFalsy();

    reviewer.addReviewed(file2);
    expect(userService.isFileReviewed(reviewer, file2)).toBeTruthy();
  });

  it('should toggle file review', () => {
    const reviewer = new Reviewer();
    const file = new File();

    file.setFilenameWithRepo('path/to/file1');
    file.setCommitId('abc');

    expect(userService.isFileReviewed(reviewer, file)).toBeFalsy();

    userService.toogleFileReview(true, reviewer, file);
    expect(userService.isFileReviewed(reviewer, file)).toBeTruthy();

    userService.toogleFileReview(false, reviewer, file);
    expect(userService.isFileReviewed(reviewer, file)).toBeFalsy();
  });

  it('gets reviewer', () => {
    const diff = new Diff();
    const reviewer = new Reviewer();

    reviewer.setEmail('bob@gmail.com');
    diff.addReviewer(reviewer);

    expect(userService.getReviewer(diff, 'bob@gmail.com')).toEqual(reviewer);
  });
});

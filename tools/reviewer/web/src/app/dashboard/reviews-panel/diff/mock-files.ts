export const files: string[] = [
  `  from firebase import firebase
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
// -------------------------------------------------------------------------
  `  from firebase import firebase
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
      return lid`
];

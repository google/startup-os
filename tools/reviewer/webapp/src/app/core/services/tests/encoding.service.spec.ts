import { Author } from '@/core/proto';
import { EncodingService } from '../encoding.service';

describe('EncodingService', () => {
  let encodingService: EncodingService;
  beforeEach(() => {
    encodingService = new EncodingService();
  });

  it('should encode and decode', () => {
    const author = new Author();
    author.setEmail('test@email.com');
    author.setNeedsAttention(true);

    const base64: string = encodingService.encodeUint8ArrayToBase64String(
      author.serializeBinary(),
    );
    const binary: Uint8Array = encodingService.decodeBase64StringToUint8Array(base64);
    const author2: Author = Author.deserializeBinary(binary);

    expect(author).toEqual(author2);
  });
});

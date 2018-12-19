import { Observable } from 'rxjs';

import { FirebaseConfig, ReviewerRegistry } from '@/core/proto';
import { EncodingService } from '@/core/services';
import { globalRegistryUrl, id } from 'src/environments/global-registry';

export class GlobalRegistry {
  static getConfig(): Observable<FirebaseConfig> {
    return new Observable(observer => {
      // Load response with reviewer registry
      this.httpGet(globalRegistryUrl).subscribe(response => {
        const protoText: string = response.fields.proto.stringValue;

        // Convert binary to ReviewerRegistry
        const encodingService = new EncodingService();
        const binary: Uint8Array = encodingService.decodeBase64StringToUint8Array(protoText);
        const reviewerRegistry: ReviewerRegistry = ReviewerRegistry.deserializeBinary(binary);

        // Find reviewer config with current id
        const reviewerIdConfigList = reviewerRegistry.getReviewerConfigList().filter(
          reviewerConfig => reviewerConfig.getId() === id,
        );
        if (reviewerIdConfigList.length < 1) {
          throw new Error(`Project with id "${id}" not found`);
        }

        // Provide firebase config
        observer.next(reviewerIdConfigList[0].getFirebaseConfig());
      });
    });
  }

  // Sends http get requests
  static httpGet(url: string): Observable<any> {
    return new Observable(observer => {
      const http = new XMLHttpRequest();
      http.onreadystatechange = () => {
        if (http.readyState === 4 && http.status === 200) {
          observer.next(JSON.parse(http.responseText));
        }
      };
      http.open('GET', url, true);
      http.send(null);
    });
  }
}

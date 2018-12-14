import { Observable } from 'rxjs';

import { FirebaseConfig, ReviewerConfig, ReviewerRegistry } from '@/core/proto';
import { EncodingService } from '@/core/services';
import { globalRegistryUrl, id } from 'src/environments/global-registry';

export class GlobalRegistry {
  static connect(): Observable<FirebaseConfig> {
    return new Observable(observer => {
      // Load ReviewerRegistry
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
        const configUrl: string = reviewerIdConfigList[0].getConfigUrl();
        this.getConfig(configUrl).subscribe(firebaseConfig => {
          observer.next(firebaseConfig);
        });
      });
    });
  }

  // Loads ReviewerConfig
  static getConfig(url: string): Observable<FirebaseConfig> {
    return new Observable(observer => {
      this.httpGet(url).subscribe(response => {
        const protoText: string = response.fields.proto.stringValue;

        // Convert binary to ReviewerConfig
        const encodingService = new EncodingService();
        const binary: Uint8Array = encodingService.decodeBase64StringToUint8Array(protoText);
        const reviewerConfig: ReviewerConfig = ReviewerConfig.deserializeBinary(binary);

        observer.next(reviewerConfig.getFirebaseConfig());
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

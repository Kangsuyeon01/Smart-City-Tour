# Smart-City-Tour
- `개발 기간`: 2022.09 ~ 2023.1
- `참여 인원` : 2
- `사용 언어` : Python, Kotlin

## Intro
- GPS와 딥러닝 기술을 결합하여 서울 시내 13개의 주요 랜드마크를 식별하고 정보를 제공하는 스마트 시티 투어 모바일 애플리케이션을 개발하였습니다. 
- 사용자는 애플리케이션에 랜드마크 이미지를 업로드함으로써 해당 장소의 상세 정보를 얻을 수 있으며, 위치 기반 서비스를 통해 현재 위치에서 가장 가까운 관광지 정보도 받을 수 있습니다.


## Dataset
- [AI-Hub 랜드마크 이미지 데이터 및 한국형 사물 이미지 데이터 ](https://aihub.or.kr/) 
- CCL(Creative Commons License)를 준수한 웹 크롤링으로 데이터 획득

| Landmark                                | Image Count |
|-----------------------------------------|-------------|
| Gyeongbokgung                           | 396         |
| Gyeonghuigung                           | 151         |
| Deoksugung                              | 338         |
| Changgyeonggung                         | 155         |
| Changdeokgung                           | 306         |
| Dongdaemun Design Plaza (DDP)           | 310         |
| Lotte World                             | 196         |
| Seodaemun Prison History Hall           | 304         |
| Coex                                    | 351         |
| Ikseon-Dong                             | 256         |
| Seoul Museum of Art                     | 100         |
| Seoul N Tower                           | 367         |
| Hongik University (Hongdae) Street      | 184         |


## Method
1. 이미지 분류를 위한 랜드마크 이미지 수집
    - 파이썬 자동화 라이브러리 활용하여 웹 크롤링을 통해 이미지 추가 수집
2. 데이터 불균형과 과적합을 해결하기 위한 데이터 증강 
  - 초기 데이터 부족에 따른 과적합 문제를 해결하기 위해, 데이터 증강 진행
  - 학습 과정에서 무작위 90도 회전, 상하 및 좌우 반전과 Cutmix 등 활용
  - 데이터 증강 이미지 예시
    ![image](https://github.com/Kangsuyeon01/Smart-City-Tour/assets/94098065/3ce3d260-895a-4eca-8f3a-89832904eee3)


3. 백본 네트워크의 성능 비교 및 검증
  - 각 증강 기법의 조합과 사전 학습된 CNN 모델의 백본 네트워크 비교실험을 
    통해 가장 성능이 좋았던 ResNet-50-D를 백본 네트워크로 활용
4. 최종 모델의 학습 및 추론 성능
  - K-Fold Validation으로 최종 모델을 추론하여, 평균 정확도와 F1 Score에
    서 높은 성능을 달성한 최종 모델로 대해 5개 모델의 소프트 보팅으로 추론함.



## Score

### 백본네트워크 실험 결과
| Backbone         | Validation Accuracy | Validation F1 Score |
|------------------|---------------------|---------------------|
| VGG19            | 0.9078              | 0.8658              |
| ResNet-50        | 0.9163              | 0.8686              |
| EfficientNet-B3  | 0.8979              | 0.8428              |
| ResNet-50-D      | 0.9321              | 0.9011              |

### 데이터 증강 실험 결과
| Method                                   | Validation Accuracy | Validation F1 Score |
|------------------------------------------|---------------------|---------------------|
| ResNet-50-D + Albumentations             | 0.9433              | 0.9222              |
| ResNet-50-D + Cutmix                     | 0.9602              | 0.9437              |
| ResNet-50-D + Albumentations + Cutmix    | 0.9602              | 0.9466              |

### 최종 모델 학습 결과
|        | Validation Accuracy | Validation F1 Score |
|--------|---------------------|---------------------|
| Fold 1 | 0.9561              | 0.9459              |
| Fold 2 | 0.9588              | 0.9431              |
| Fold 3 | 0.9547              | 0.9323              |
| Fold 4 | 0.9574              | 0.9304              |
| Fold 5 | 0.9616              | 0.9430              |
| Average| 0.9577              | 0.9389              |

---
## Result
- ResNet-D 모델을 사용해 13개 서울 랜드마크에 대해 학습
- 최종적으로 평균 정확도 0.957과 F1-Score 0.938 달성
- 학습한 모델과 안드로이드 애플리케이션에서 소켓통신을 통한 서비스 구현

---
## Demo
### Flowchart of Application

![image](https://github.com/Kangsuyeon01/Smart-City-Tour/assets/94098065/caed8a59-cf26-4ec9-a8ff-c8875eacf689)

- 애플리케이션 초기 메인화면
![image](https://github.com/Kangsuyeon01/Smart-City-Tour/assets/94098065/92a130e9-abe7-415d-92d5-145f052a6dbc)

- 애플리케이션 사용 화면
![image](https://github.com/Kangsuyeon01/Smart-City-Tour/assets/94098065/2afd5156-4a56-4d6c-8f10-4c88da3876c1)
---


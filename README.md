# Blog API Documentation

## Overview
이 문서는 블로그 만들기 과제에서 사용 가능한 엔드포인트와 이를 다루는 방법에 대한 개요를 제공합니다.

---

## Endpoints

### 1. 회원 가입(A)
#### **Request**
- **Method:** `POST`
- **URL:** `/users`
- **Body:**
```json
{
  "email": "email@urssu.com",
  "password": "password",
  "username": "username"
}
```
- **Description:** 비밀번호를 암호화해서 저장한다.
#### **Example Request**
```bash
curl -X POST http://localhost:8080/users
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "email": "email@urssu.com",
  "username": "username"
}
```

---

### 2. 전체 회원 목록 조회

#### **Request**
- **Method:** `GET`
- **URL:** `/users`
- **Description:** 전체 회원의 목록을 조회한다.
- #### **Example Request**
```bash
curl -X GET http://localhost:8080/users
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
[
  {
    "email": "email@urssu.com",
    "username": "username",
    "id": 1
  },
  {
    "email": "2email@urssu.com",
    "username": "2username",
    "id": 2
  },
  {
    "email": "3email@urssu.com",
    "username": "3username",
    "id": 3
  }
]
```

---

### 3. 특정 회원 조회
#### **Request**
- **Method:** `GET`
- **URL:** `/users/{id}`
- **Description:** 특정한 회원에 대한 정보를 조회할 수 있다. 암호화된 password 반환한다.
- #### **Example Request**
```bash
curl -X GET http://localhost:8080/users/{id}
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "id": 1,
  "email": "email@urssu.com",
  "username": "username",
  "password": "$2a$10$q6zCBdY9Gla/LrXD0s7xO..asmi3332PU7fARsClMUXVW8SzQTP.y",
  "articles": [],
  "comments": []
}
```

---
### 4. 회원 탈퇴(H)
#### **Request**
- **Method:** `DELETE`
- **URL:** `/users/{id}`
- **Body:**
```json
{
  "email" : "email@urssu.com",
  "password" : "password"
}
```
- **Description:** 회원 탈퇴 시 해당 회원과 연관된 데이터(게시글, 댓글)도 삭제된다.
#### **Example Request**
```bash
curl -X DELETE http://localhost:8080/users/{id}
```

#### **Response**
- **Status Code:** `200 OK`

---

### 5. 게시글 작성하기(B)
#### **Request**
- **Method:** `POST`
- **URL:** `/articles`
- **Body:**
```json
{
  "email" : "email@urssu.com",
  "password" : "password",
  "title" : "title",
  "content" : "content"
}
```
- **Description:** title, content 필드는 "", " ", null 값을 허용하지 않는다.
#### **Example Request**
```bash
curl -X POST http://localhost:8080/articles
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "articleId" : 1,
  "email" : "email@urssu.com",
  "title" : "title",
  "content" : "content"
}
```

---

### 6. 게시글 조회
#### **Request**
- **Method:** `GET`
- **URL:** `/articles`
- **Description:** 모든 게시글을 조회한다.
- #### **Example Request**
```bash
curl -X GET http://localhost:8080/articles
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
[
  {
    "articleId": 1,
    "email": "test@example.com",
    "title": "Test Article",
    "content": "This is a test article."
  },
  {
    "articleId": 2,
    "email": "email@urssu.com",
    "title": "title",
    "content": "content"
  }
]
```

---

### 7. 특정 게시글 조회
#### **Request**
- **Method:** `GET`
- **URL:** `/articles/{id}`
- **Description:** 모든 게시글을 조회한다.
- #### **Example Request**
```bash
curl -X GET http://localhost:8080/articles/{id}
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "articleId": 1,
  "email": "email@urssu.com",
  "title": "title",
  "content": "content"
}
```

---

### 8. 게시글 수정하기(C)
#### **Request**
- **Method:** `PUT`
- **URL:** `/articles/{id}`
- **Body:**
```json
{
  "email" : "email@urssu.com",
  "password" : "password",
  "title" : "title",
  "content" : "content"
}
```
- **Description:** title, content 필드는 "", " ", null 값을 허용하지 않습니다. Request Body의 email, password와 일치하는 자신의 게시글만 수정 가능합니다.
#### **Example Request**
```bash
curl -X PUT http://localhost:8080/articles/{id}
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "articleId" : 1,
  "email" : "email@urssu.com",
  "title" : "title",
  "content" : "content"
}
```

---

### 9. 게시글 삭제하기(D)
#### **Request**
- **Method:** `DELETE`
- **URL:** `/articles/{id}`
- **Body:**
```json
{
  "email" : "email@urssu.com",
  "password" : "password"
}
```
- **Description:** 특정 게시물을 삭제할 수 있습니다.
#### **Example Request**
```bash
curl -X DELETE http://localhost:8080/articles/{id}
```

#### **Response**
- **Status Code:** `200 OK`

---

### 10. 댓글 작성하기(E)
#### **Request**
- **Method:** `POST`
- **URL:** `/articles/{articleId}/comments`
- **Body:**
```json
{
  "email" : "email@urssu.com",
  "password" : "password",
  "content" : "content"
}
```
- **Description:** content 필드는 "", " ", null을 허용하지 않습니다.
#### **Example Request**
```bash
curl -X POST http://localhost:8080/articles/{articleId}/comments
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "commentId" : 1,
  "email" : "email@urssu.com",
  "content" : "content"
}
```

---

### 11. 댓글 확인하기
#### **Request**
- **Method:** `GET`
- **URL:** `/articles/{articleId}/comments/`
- **Description:** 특정 게시물의 댓글을 확인할 수 있습니다.
- #### **Example Request**
```bash
curl -X GET http://localhost:8080/articles/{articleId}/comments/
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "commentId" : 1,
  "email" : "email@urssu.com",
  "content" : "content"
}
```

---

### 12. 댓글 수정하기(F)
#### **Request**
- **Method:** `PUT`
- **URL:** `/articles/{articleId}/comments/{commentId}`
- **Body:**
```json
{
  "email" : "email@urssu.com",
  "password" : "password",
  "content" : "content"
}
```
- **Description:** content 필드는 "", " ", null을 허용하지 않습니다. Request Body의 email, password와 일치하는 자신의 댓글만 수정할 수 있습니다.
#### **Example Request**
```bash
curl -X PUT http://localhost:8080/articles/{articleId}/comments/{commentId}
```

#### **Response**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "commentId" : 1,
  "email" : "email@urssu.com",
  "content" : "content"
}
```

---

### 13. 댓글 삭제하기(G)
#### **Request**
- **Method:** `DELETE`
- **URL:** `/articles/{articleId}/comments/{commentId}`
- **Body:**
```json
{
  "email" : "email@urssu.com",
  "password" : "password"
}
```
- **Description:** Request Body의 email, password와 일치하는 자신의 댓글만 삭제할 수 있습니다.
#### **Example Request**
```bash
curl -X DELETE http://localhost:8080/articles/{articleId}/comments/{commentId}
```

#### **Response**
- **Status Code:** `200 OK`

---


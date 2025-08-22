package com.blog.application.entity;


import jakarta.persistence.*; // 패키지 내의 모든 클래스를 import하는 선언
// 데이터베이스와의 객체-관계 매핑(ORM)을 위한 표준 API를 사용할 수 있게 된다

@Entity
@Table(name="\"article\"") // 테이블명이 article (따옴표로 예약어 회피
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;
    // 각 게시글은 반드시 작성자(User)를 가져야 하며, LAZY 로딩 적용
    @ManyToOne(fetch = FetchType.LAZY)
    // LAZY(지연 로딩) : 관련된 엔티티를 실제로 필요할 때까지 데이터베이스에서 조회하지 않는 전략, 반대는 EAGER 로딩임
    // 예를 들어, Article 객체를 조회할 때 연관된 User 엔티티는 바로 가져오지 않고, getUser()를 호출하는 순간 로딩된다
    // n+1 : 한 게시물에 n개의 댓글이 달리면, 연관관계가 맺어지면 JPA 게시글 조회 이후에 따로 select를 날리는데 그럼 n+1을 날리기 때문에 db상 좋지 않음,
    // 즉시 로딩으로 하면 이러한 문제가 발생할 수 있음, DB 성능의 저하가 될 수 있음 -> 최적할 수 있는 방안들 -> fetch join은 이걸 해결하는 방안 중 하나
    @JoinColumn(name = "user_id", nullable = false) //엔티티의 필드를 데이터베이스 테이블의 컬럼과 매핑할 때 사용하는 속성을 정의, 이메일처럼 중복되면 안 되는 데이터를 관리할 때 사용
    private User user;

    // 기본 생성자 (JPA 필수)
    public Article() {
    }

    // 생성자: 제목, 내용, 작성자 설정
    public Article(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.likeCount = 0L; // 초기값 설정
    }

    // Getter & Setter 메서드들
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public Long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    
    // 비즈니스 메서드: 좋아요 개수 증가
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    // 비즈니스 메서드: 좋아요 개수 감소
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}

//Article 엔티티는 게시글 정보를 나타내며, 제목, 내용, 그리고 작성자(User)와의 연관관계를 포함한다
//데이터베이스의 "article" 테이블과 매핑되어 있으며, 게시글 CRUD 시 기본 데이터 모델로 사용된다


# NGO VAN DUC - CV Rewrite Notes

## Recruiter View

CV nen nhan manh 3 diem:

- Backend Java/Spring Boot developer co kinh nghiem toi uu hieu nang va benchmark thuc te.
- Lam duoc full flow tu code, database, cache, benchmark den phan tich bottleneck.
- Co tu duy do luong bang so lieu thay vi chi mo ta chuc nang.

## Suggested Professional Summary

Backend Developer voi kinh nghiem xay dung va toi uu ung dung web bang Java, Spring Boot, PostgreSQL, Redis va REST API. Co kha nang phan tich bottleneck, thiet ke benchmark, toi uu truy van va cai thien throughput cho he thong co tai dong thoi cao. Da tham gia cac bai toan donation flow, wallet, realtime notification va performance tuning theo huong do luong bang metric thuc te.

## Suggested Performance Section

### Performance Engineering

- Thiet ke va thuc thi benchmark cho donation flow bang k6 va script test concurrency de danh gia do tre, throughput va ty le loi.
- Phan tich hot path cua he thong donation, xac dinh cac bottleneck chinh o database connection pool, repeated lookup va latency tren request path.
- Toi uu backend bang cach dua lookup tan suat cao vao cache, giam round-trip database trong luong xu ly donation.
- Danh gia hieu nang he thong theo cac chi so P50, P95, P99 latency, ops/sec, failure rate, CPU, memory va database load.
- De xuat chien luoc tuning Hikari pool, Tomcat threads, cache lookup va database index dua tren ket qua benchmark thuc te.
- Co kinh nghiem doc va dien giai ket qua benchmark de dua ra nguong capacity va khuyen nghi scale cho he thong.

## Suggested Project Rewrite

### Donate Web Backend

Tech stack: Java, Spring Boot, PostgreSQL, Redis, WebSocket, REST API

- Phat trien backend cho he thong donation, wallet va notification realtime.
- Xay dung cac API cho donation qua QR, wallet va webhook settlement.
- Thuc hien benchmark cho donation flow bang cac script concurrency test va k6 de do throughput, latency va failure rate.
- Phan tich bottleneck trong request path, xac dinh anh huong cua database connection pool, repeated entity lookup va logging overhead.
- Toi uu hot path bang cache lookup cho payment method va streamer entity, gop phan giam latency request.
- De xuat cac huong toi uu tiep theo nhu index cho donation lookup, tuning pool size va tach side effect khoi request path.

## Stronger Achievement Style Bullets

Neu ban muon CV manh hon, uu tien cach viet theo ket qua:

- Built and benchmarked a high-concurrency donation flow using Spring Boot, PostgreSQL, Redis and k6.
- Identified backend bottlenecks through latency and throughput analysis, then improved hot-path efficiency by reducing repeated database lookups.
- Designed load test scenarios to evaluate system stability under hundreds to thousands of concurrent users.
- Measured and interpreted P50, P95, P99 latency, failure rate and ops/sec to support performance tuning decisions.

## Suggested Skills Section

- Backend: Java, Spring Boot, REST API, JPA, Hibernate
- Database: PostgreSQL, SQL optimization, indexing
- Cache and Messaging: Redis, WebSocket
- Performance: k6, load testing, concurrency testing, bottleneck analysis, latency analysis, throughput measurement
- Tools: Git, Postman, Gradle

## Short Performance Block For TopCV

Neu TopCV khong con nhieu cho, co the dan ngan gon doan nay:

Performance: Co kinh nghiem benchmark va toi uu backend voi k6, concurrency test, Redis cache, Hikari pool tuning, latency analysis (P50/P95/P99), throughput measurement va xac dinh bottleneck trong donation flow.

## Hiring Manager Notes

De CV thuyet phuc hon, nen uu tien:

- Viet ro ten vai tro mong muon: Backend Developer hoac Backend Java Developer.
- Moi project nen co 3-5 bullet theo dang action + impact.
- Uu tien tu khoa ma nha tuyen dung tim: Spring Boot, PostgreSQL, Redis, REST API, performance tuning, load testing, caching, concurrency.
- Neu co so lieu that, them 1-2 dong co metric, vi du: benchmark den 500/1000 concurrent users, failure rate < 1 percent, p95 latency duoi nguong muc tieu.

## Next Best Step

Neu ban gui ban editable nhu DOCX, Markdown, hoac paste text CV vao day, co the rewrite lai tung muc theo giong recruiter va chinh sat vao file goc.

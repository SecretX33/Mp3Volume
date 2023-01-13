# Unique Criterea: globally

## ID generation with seed (all parameters random)

### Stats

**Unique criterea:** Globally unique

**Amount of IDs generated:** 50.000.000

**Pool Size:** 2.102.483.674 (result of 2.147.483.647 - 45.000.000)

**Seed uses:**

- companyId (randomly generated for each number)
- responseId (randomly generated for each number)
- userId (randomly generated for each number)

### Results

These are the results of 25 runs generating the ids 

1. Conflict detected! 590,837 numbers repeated!
2. Conflict detected! 590,247 numbers repeated!
3. Conflict detected! 590,449 numbers repeated!
4. Conflict detected! 591,576 numbers repeated!
5. Conflict detected! 591,399 numbers repeated!
6. Conflict detected! 590,954 numbers repeated!
7. Conflict detected! 590,232 numbers repeated!
8. Conflict detected! 590,365 numbers repeated!
9. Conflict detected! 591,245 numbers repeated!
10. Conflict detected! 588,740 numbers repeated!
11. Conflict detected! 589,554 numbers repeated!
12. Conflict detected! 590,711 numbers repeated!
13. Conflict detected! 591,026 numbers repeated!
14. Conflict detected! 590,177 numbers repeated!
15. Conflict detected! 589,742 numbers repeated!
16. Conflict detected! 589,976 numbers repeated!
17. Conflict detected! 590,310 numbers repeated!
18. Conflict detected! 591,353 numbers repeated!
19. Conflict detected! 590,401 numbers repeated!
20. Conflict detected! 590,279 numbers repeated!
21. Conflict detected! 590,781 numbers repeated!
22. Conflict detected! 589,015 numbers repeated!
23. Conflict detected! 590,480 numbers repeated!
24. Conflict detected! 590,374 numbers repeated!
25. Conflict detected! 590,383 numbers repeated!

## ID generation with seed (cycled companyId)

### Stats

**Unique criterea:** Globally unique

**Amount of IDs generated:** 50.000.000

**Pool Size:** 2.102.483.674 (result of 2.147.483.647 - 45.000.000)

**Seed uses:**

- companyId (diligently cycled between 0 until 500)
- responseId (randomly generated for each number)
- userId (randomly generated for each number)

### Results

These are the results of 25 runs generating the ids

1. Conflict detected! 590,467 numbers repeated!
2. Conflict detected! 589,384 numbers repeated!
3. Conflict detected! 589,661 numbers repeated!
4. Conflict detected! 590,158 numbers repeated!
5. Conflict detected! 590,872 numbers repeated!
6. Conflict detected! 592,115 numbers repeated!
7. Conflict detected! 589,411 numbers repeated!
8. Conflict detected! 590,839 numbers repeated!
9. Conflict detected! 589,584 numbers repeated!
10. Conflict detected! 590,610 numbers repeated!
11. Conflict detected! 592,404 numbers repeated!
12. Conflict detected! 590,821 numbers repeated!
13. Conflict detected! 591,092 numbers repeated!
14. Conflict detected! 591,331 numbers repeated!
15. Conflict detected! 590,609 numbers repeated!
16. Conflict detected! 590,899 numbers repeated!
17. Conflict detected! 590,034 numbers repeated!
18. Conflict detected! 590,730 numbers repeated!
19. Conflict detected! 590,937 numbers repeated!
20. Conflict detected! 591,482 numbers repeated!
21. Conflict detected! 590,320 numbers repeated!
22. Conflict detected! 588,980 numbers repeated!
23. Conflict detected! 590,296 numbers repeated!
24. Conflict detected! 590,621 numbers repeated!
25. Conflict detected! 591,226 numbers repeated!

## ID generation without seed

### Stats

**Unique criterea:** Globally unique

**Amount of IDs generated:** 50.000.000

**Pool Size:** 2.102.483.674 (result of 2.147.483.647 - 45.000.000)

**Seed uses:** None (used Java `ThreadLocalRandom.current()`)

### Results

These are the results of 25 runs generating the ids

1. Conflict detected! 591,887 numbers repeated!
2. Conflict detected! 590,154 numbers repeated!
3. Conflict detected! 589,971 numbers repeated!
4. Conflict detected! 590,675 numbers repeated!
5. Conflict detected! 590,244 numbers repeated!
6. Conflict detected! 588,801 numbers repeated!
7. Conflict detected! 590,144 numbers repeated!
8. Conflict detected! 589,715 numbers repeated!
9. Conflict detected! 588,857 numbers repeated!
10. Conflict detected! 589,762 numbers repeated!
11. Conflict detected! 590,075 numbers repeated!
12. Conflict detected! 590,221 numbers repeated!
13. Conflict detected! 590,482 numbers repeated!
14. Conflict detected! 589,757 numbers repeated!
15. Conflict detected! 589,093 numbers repeated!
16. Conflict detected! 589,429 numbers repeated!
17. Conflict detected! 589,164 numbers repeated!
18. Conflict detected! 589,386 numbers repeated!
19. Conflict detected! 589,686 numbers repeated!
20. Conflict detected! 590,545 numbers repeated!
21. Conflict detected! 589,593 numbers repeated!
22. Conflict detected! 589,056 numbers repeated!
23. Conflict detected! 588,862 numbers repeated!
24. Conflict detected! 589,282 numbers repeated!
25. Conflict detected! 590,449 numbers repeated!

# Unique Criterea: per company

## ID generation with seed (cycled company)

### Stats

**Unique criterea:** Unique per company

**Amount of IDs generated:** 50.000.000

**Pool Size:** 2.102.483.674 (result of 2.147.483.647 - 45.000.000)

**Seed uses:**

- companyId (diligently cycled between 0 until 500)
- responseId (randomly generated for each number)
- userId (randomly generated for each number)

### Results

These are the results of 25 runs generating the ids

1. Conflict detected! 1,921 numbers repeated!
2. Conflict detected! 1,916 numbers repeated!
3. Conflict detected! 2,057 numbers repeated!
4. Conflict detected! 2,017 numbers repeated!
5. Conflict detected! 2,026 numbers repeated!
6. Conflict detected! 2,136 numbers repeated!
7. Conflict detected! 1,990 numbers repeated!
8. Conflict detected! 1,993 numbers repeated!
9. Conflict detected! 1,978 numbers repeated!
10. Conflict detected! 2,095 numbers repeated!
11. Conflict detected! 1,893 numbers repeated!
12. Conflict detected! 2,081 numbers repeated!
13. Conflict detected! 2,062 numbers repeated!
14. Conflict detected! 2,019 numbers repeated!
15. Conflict detected! 2,012 numbers repeated!
16. Conflict detected! 1,994 numbers repeated!
17. Conflict detected! 1,990 numbers repeated!
18. Conflict detected! 2,046 numbers repeated!
19. Conflict detected! 1,907 numbers repeated!
20. Conflict detected! 1,938 numbers repeated!
21. Conflict detected! 1,988 numbers repeated!
22. Conflict detected! 2,044 numbers repeated!
23. Conflict detected! 1,994 numbers repeated!
24. Conflict detected! 2,009 numbers repeated!
25. Conflict detected! 2,055 numbers repeated!

## ID generation without seed

### Stats

**Unique criterea:** Unique per company

**Amount of IDs generated:** 50.000.000

**Pool Size:** 2.102.483.674 (result of 2.147.483.647 - 45.000.000)

**Seed uses:** None (used Java `ThreadLocalRandom.current()`)

### Results

These are the results of 25 runs generating the ids

1. Conflict detected! 1,207 numbers repeated!
2. Conflict detected! 1,243 numbers repeated!
3. Conflict detected! 1,171 numbers repeated!
4. Conflict detected! 1,140 numbers repeated!
5. Conflict detected! 1,169 numbers repeated!
6. Conflict detected! 1,163 numbers repeated!
7. Conflict detected! 1,246 numbers repeated!
8. Conflict detected! 1,201 numbers repeated!
9. Conflict detected! 1,125 numbers repeated!
10. Conflict detected! 1,164 numbers repeated!
11. Conflict detected! 1,243 numbers repeated!
12. Conflict detected! 1,179 numbers repeated!
13. Conflict detected! 1,202 numbers repeated!
14. Conflict detected! 1,205 numbers repeated!
15. Conflict detected! 1,147 numbers repeated!
16. Conflict detected! 1,228 numbers repeated!
17. Conflict detected! 1,175 numbers repeated!
18. Conflict detected! 1,228 numbers repeated!
19. Conflict detected! 1,166 numbers repeated!
20. Conflict detected! 1,139 numbers repeated!
21. Conflict detected! 1,193 numbers repeated!
22. Conflict detected! 1,145 numbers repeated!
23. Conflict detected! 1,206 numbers repeated!
24. Conflict detected! 1,218 numbers repeated!
25. Conflict detected! 1,165 numbers repeated!

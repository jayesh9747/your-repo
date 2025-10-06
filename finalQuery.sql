SELECT
  p.amount AS salary,
  CONCAT(e.first_name, ' ', e.last_name) AS name,
  TIMESTAMPDIFF(YEAR, e.dob, DATE(p.payment_time)) AS age,
  d.department_name
FROM payments p
JOIN employee e ON e.emp_id = p.emp_id
JOIN department d ON d.department_id = e.department
WHERE DAY(p.payment_time) <> 1
ORDER BY p.amount DESC
LIMIT 1;

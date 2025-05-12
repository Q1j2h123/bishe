-- 测试用户数据
INSERT INTO user (id, user_account, user_password, user_name, user_avatar, user_role, gender, phone, email, user_status, create_time, update_time)
VALUES
(100, 'testuser0', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 0', '', 'user', 1, '', '', 0, NOW(), NOW()),
(101, 'testuser1', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 1', '', 'user', 1, '', '', 0, NOW(), NOW()),
(102, 'testuser2', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 2', '', 'user', 1, '', '', 0, NOW(), NOW()),
(103, 'testuser3', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 3', '', 'user', 1, '', '', 0, NOW(), NOW()),
(104, 'testuser4', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 4', '', 'user', 1, '', '', 0, NOW(), NOW()),
(105, 'testuser5', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 5', '', 'user', 1, '', '', 0, NOW(), NOW()),
(106, 'testuser6', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 6', '', 'user', 1, '', '', 0, NOW(), NOW()),
(107, 'testuser7', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 7', '', 'user', 1, '', '', 0, NOW(), NOW()),
(108, 'testuser8', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 8', '', 'user', 1, '', '', 0, NOW(), NOW()),
(109, 'testuser9', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'Test User 9', '', 'user', 1, '', '', 0, NOW(), NOW());

-- 测试题目数据（编程题）
INSERT INTO program_problem (id, title, description, difficulty, time_limit, memory_limit, languages, position_type, template_code)
VALUES
(100, '两数之和 - 测试', '给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出和为目标值的那两个整数，并返回它们的数组下标。你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。', 'EASY', 1000, 128, 'JAVA,PYTHON,CPP', '后端', '// Java代码模板\npublic class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // 请在此处编写代码\n        return null;\n    }\n}');

-- 测试题目数据（选择题）
INSERT INTO choice_problem (id, title, description, difficulty, options, answer)
VALUES
(101, '以下哪个不是Java的基本数据类型 - 测试', 'Java中有八种基本数据类型，请选出其中不属于Java基本数据类型的一项。', 'EASY', '[{"key":"A","value":"byte"},{"key":"B","value":"short"},{"key":"C","value":"String"},{"key":"D","value":"boolean"}]', '[2]');

-- 测试题目数据（判断题）
INSERT INTO judge_problem (id, title, description, difficulty, answer)
VALUES
(102, 'Java是一种编译型语言 - 测试', 'Java是一种编译型语言，请判断这句话的正确性。', 'EASY', false);

-- 测试提交数据（编程题）
INSERT INTO program_submission (id, user_id, problem_id, code, language, result, submit_time, execution_time, memory_used, error_message)
VALUES
(100, 100, 100, 'public class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        for (int i = 0; i < nums.length; i++) {\n            for (int j = i + 1; j < nums.length; j++) {\n                if (nums[i] + nums[j] == target) {\n                    return new int[] { i, j };\n                }\n            }\n        }\n        return null;\n    }\n}', 'JAVA', 'ACCEPTED', NOW(), 10, 32, '');

-- 测试提交数据（选择题）
INSERT INTO choice_judge_submission (id, user_id, problem_id, selected_options, result, submit_time)
VALUES
(101, 100, 101, '[2]', 'ACCEPTED', NOW());

-- 测试题目与标签关联
INSERT INTO problem (id, title, description, difficulty, type, create_time, update_time, status, position_type, tags)
VALUES
(100, '两数之和 - 测试', '给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出和为目标值的那两个整数，并返回它们的数组下标。', 'EASY', 'PROGRAM', NOW(), NOW(), 0, '后端', '["数组","哈希表"]'),
(101, '以下哪个不是Java的基本数据类型 - 测试', 'Java中有八种基本数据类型，请选出其中不属于Java基本数据类型的一项。', 'EASY', 'CHOICE', NOW(), NOW(), 0, '后端', '["Java","基础"]'),
(102, 'Java是一种编译型语言 - 测试', 'Java是一种编译型语言，请判断这句话的正确性。', 'EASY', 'JUDGE', NOW(), NOW(), 0, '后端', '["Java","基础"]');

-- 测试用户提交记录
INSERT INTO submission (id, user_id, problem_id, submission_type, result, submission_time)
VALUES
(100, 100, 100, 'PROGRAM', 'ACCEPTED', NOW() - INTERVAL 1 DAY),
(101, 100, 101, 'CHOICE', 'ACCEPTED', NOW() - INTERVAL 2 DAY),
(102, 101, 100, 'PROGRAM', 'WRONG_ANSWER', NOW() - INTERVAL 3 DAY),
(103, 102, 100, 'PROGRAM', 'TIME_LIMIT_EXCEEDED', NOW() - INTERVAL 4 DAY),
(104, 103, 101, 'CHOICE', 'WRONG_ANSWER', NOW() - INTERVAL 5 DAY),
(105, 104, 102, 'JUDGE', 'ACCEPTED', NOW() - INTERVAL 6 DAY);

-- 测试用户题目状态
INSERT INTO user_problem_status (id, user_id, problem_id, status, solved_time, attempt_times)
VALUES
(100, 100, 100, 'SOLVED', NOW() - INTERVAL 1 DAY, 1),
(101, 100, 101, 'SOLVED', NOW() - INTERVAL 2 DAY, 1),
(102, 101, 100, 'ATTEMPTED', NULL, 1),
(103, 102, 100, 'ATTEMPTED', NULL, 1),
(104, 103, 101, 'ATTEMPTED', NULL, 1),
(105, 104, 102, 'SOLVED', NOW() - INTERVAL 6 DAY, 1); 
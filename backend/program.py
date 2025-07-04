import pandas as pd

# 定义十道编程题数据
new_questions = [
    {
        "题目标题": "计算数组元素之和",
        "题目内容": "给定一个整数数组，计算数组中所有元素的总和。",
        "函数名称": "public int sumOfArray(int[] nums) {    // 请在此实现计算数组元素之和算法    return 0;}",
        "参数类型": "int[]",
        "返回值类型": "int",
        "测试用例输入": "[1, 2, 3]",
        "测试用例输出": "6",
        "Java代码模板": "public int sumOfArray(int[] nums) {    int sum = 0;    // 请在此补充完整计算数组元素之和的代码逻辑    return sum;}",
        "Python代码模板": "def sumOfArray(nums):    sum_num = 0    # 请在此补充完整计算数组元素之和的代码逻辑    return sum_num",
        "C++代码模板": "#include <vector>int sumOfArray(std::vector<int>& nums) {    int sum = 0;    // 请在此补充完整计算数组元素之和的代码逻辑    return sum;}",
        "C代码模板": "#include <stdlib.h>int sumOfArray(int* nums, int numsSize) {    int sum = 0;    // 请在此补充完整计算数组元素之和的代码逻辑    return sum;}",
        "Java标准答案": "public int sumOfArray(int[] nums) {    int sum = 0;    for (int num : nums) {        sum += num;    }    return sum;}",
        "Python标准答案": "def sumOfArray(nums):    return sum(nums)",
        "C++标准答案": "#include <vector>int sumOfArray(std::vector<int>& nums) {    int sum = 0;    for (int num : nums) {        sum += num;    }    return sum;}",
        "C标准答案": "#include <stdlib.h>int sumOfArray(int* nums, int numsSize) {    int sum = 0;    for (int i = 0; i < numsSize; i++) {        sum += nums[i];    }    return sum;}",
        "时间限制(ms)": 1000,
        "内存限制(MB)": 128,
        "难度": "EASY",
        "岗位类型": "BACKEND",
        "标签": "编程题,数组,求和"
    },
    {
        "题目标题": "判断字符串是否为回文串",
        "题目内容": "判断一个给定的字符串是否是回文串，即正向和反向读取都相同。",
        "函数名称": "public boolean isPalindromeString(String str) {    // 请在此实现判断字符串是否为回文串的算法    return false;}",
        "参数类型": "String",
        "返回值类型": "boolean",
        "测试用例输入": "\"level\"",
        "测试用例输出": "true",
        "Java代码模板": "public boolean isPalindromeString(String str) {    // 请在此补充完整判断字符串是否为回文串的代码逻辑    return false;}",
        "Python代码模板": "def isPalindromeString(str):    # 请在此补充完整判断字符串是否为回文串的代码逻辑    return False",
        "C++代码模板": "#include <string>bool isPalindromeString(const std::string& str) {    // 请在此补充完整判断字符串是否为回文串的代码逻辑    return false;}",
        "C代码模板": "#include <stdbool.h>#include <string.h>bool isPalindromeString(const char* str) {    // 请在此补充完整判断字符串是否为回文串的代码逻辑    return false;}",
        "Java标准答案": "public boolean isPalindromeString(String str) {    int left = 0;    int right = str.length() - 1;    while (left < right) {        if (str.charAt(left) != str.charAt(right)) {            return false;        }        left++;        right--;    }    return true;}",
        "Python标准答案": "def isPalindromeString(str):    return str == str[::-1]",
        "C++标准答案": "#include <string>bool isPalindromeString(const std::string& str) {    int left = 0;    int right = str.length() - 1;    while (left < right) {        if (str[left] != str[right]) {            return false;        }        left++;        right--;    }    return true;}",
        "C标准答案": "#include <stdbool.h>#include <string.h>bool isPalindromeString(const char* str) {    int len = strlen(str);    int left = 0;    int right = len - 1;    while (left < right) {        if (str[left] != str[right]) {            return false;        }        left++;        right--;    }    return true;}",
        "时间限制(ms)": 1000,
        "内存限制(MB)": 128,
        "难度": "EASY",
        "岗位类型": "FRONTEND",
        "标签": "编程题,字符串,回文串"
    },
    {
        "题目标题": "计算列表中奇数的个数",
        "题目内容": "给定一个整数列表，统计其中奇数的数量。",
        "函数名称": "public int countOdds(List<Integer> list) {    // 请在此实现统计列表中奇数个数的算法    return 0;}",
        "参数类型": "List<Integer>",
        "返回值类型": "int",
        "测试用例输入": "[1, 2, 3, 4, 5]",
        "测试用例输出": "3",
        "Java代码模板": "import java.util.List;public int countOdds(List<Integer> list) {    int count = 0;    // 请在此补充完整统计列表中奇数个数的代码逻辑    return count;}",
        "Python代码模板": "def countOdds(list):    count = 0    // 请在此补充完整统计列表中奇数个数的代码逻辑    return count",
        "C++代码模板": "#include <vector>int countOdds(const std::vector<int>& list) {    int count = 0;    // 请在此补充完整统计列表中奇数个数的代码逻辑    return count;}",
        "C代码模板": "#include <stdlib.h>int countOdds(int* list, int listSize) {    int count = 0;    // 请在此补充完整统计列表中奇数个数的代码逻辑    return count;}",
        "Java标准答案": "import java.util.List;public int countOdds(List<Integer> list) {    int count = 0;    for (int num : list) {        if (num % 2 != 0) {            count++;        }    }    return count;}",
        "Python标准答案": "def countOdds(list):    return sum(1 for num in list if num % 2 != 0)",
        "C++标准答案": "#include <vector>int countOdds(const std::vector<int>& list) {    int count = 0;    for (int num : list) {        if (num % 2 != 0) {            count++;        }    }    return count;}",
        "C标准答案": "#include <stdlib.h>int countOdds(int* list, int listSize) {    int count = 0;    for (int i = 0; i < listSize; i++) {        if (list[i] % 2 != 0) {            count++;        }    }    return count;}",
        "时间限制(ms)": 1000,
        "内存限制(MB)": 128,
        "难度": "EASY",
        "岗位类型": "BACKEND",
        "标签": "编程题,列表,奇数统计"
    },
    {
        "题目标题": "计算两个整数的最大公约数",
        "题目内容": "编写函数计算两个整数的最大公约数。",
        "函数名称": "public int gcd(int a, int b) {    // 请在此实现计算两个整数最大公约数的算法    return 0;}",
        "参数类型": "int, int",
        "返回值类型": "int",
        "测试用例输入": "12, 18",
        "测试用例输出": "6",
        "Java代码模板": "public int gcd(int a, int b) {    // 请在此补充完整计算两个整数最大公约数的代码逻辑    return 0;}",
        "Python代码模板": "def gcd(a, b):    // 请在此补充完整计算两个整数最大公约数的代码逻辑    return 0",
        "C++代码模板": "int gcd(int a, int b) {    // 请在此补充完整计算两个整数最大公约数的代码逻辑    return 0;}",
        "C代码模板": "int gcd(int a, int b) {    // 请在此补充完整计算两个整数最大公约数的代码逻辑    return 0;}",
        "Java标准答案": "public int gcd(int a, int b) {    while (b != 0) {        int temp = b;        b = a % b;        a = temp;    }    return a;}",
        "Python标准答案": "def gcd(a, b):    while b:        a, b = b, a % b    return a",
        "C++标准答案": "int gcd(int a, int b) {    while (b != 0) {        int temp = b;        b = a % b;        a = temp;    }    return a;}",
        "C标准答案": "int gcd(int a, int b) {    while (b != 0) {        int temp = b;        b = a % b;        a = temp;    }    return a;}",
        "时间限制(ms)": 1000,
        "内存限制(MB)": 128,
        "难度": "MEDIUM",
        "岗位类型": "BACKEND",
        "标签": "编程题,数学,最大公约数"
    },
    {
        "题目标题": "反转数组",
        "题目内容": "将给定的整数数组进行反转。",
        "函数名称": "public void reverseArray(int[] nums) {    // 请在此实现反转数组的算法    }",
        "参数类型": "int[]",
        "返回值类型": "void",
        "测试用例输入": "[1, 2, 3, 4, 5]",
        "测试用例输出": "[5, 4, 3, 2, 1]",
        "Java代码模板": "public void reverseArray(int[] nums) {    // 请在此补充完整反转数组的代码逻辑    }",
        "Python代码模板": "def reverseArray(nums):    // 请在此补充完整反转数组的代码逻辑    ",
        "C++代码模板": "#include <vector>void reverseArray(std::vector<int>& nums) {    // 请在此补充完整反转数组的代码逻辑    }",
        "C代码模板": "#include <stdlib.h>void reverseArray(int* nums, int numsSize) {    // 请在此补充完整反转数组的代码逻辑    }",
        "Java标准答案": "public void reverseArray(int[] nums) {    int left = 0;    int right = nums.length - 1;    while (left < right) {        int temp = nums[left];        nums[left] = nums[right];        nums[right] = temp;        left++;        right--;    }    }",
        "Python标准答案": "def reverseArray(nums):    nums.reverse()",
        "C++标准答案": "#include <vector>void reverseArray(std::vector<int>& nums) {    int left = 0;    int right = nums.size() - 1;    while (left < right) {        int temp = nums[left];        nums[left] = nums[right];        nums[right] = temp;        left++;        right--;    }    }",
        "C标准答案": "#include <stdlib.h>void reverseArray(int* nums, int numsSize) {    int left = 0;    int right = numsSize - 1;    while (left < right) {        int temp = nums[left];        nums[left] = nums[right];        nums[right] = temp;        left++;        right--;    }    }",
        "时间限制(ms)": 1000,
        "内存限制(MB)": 128,
        "难度": "MEDIUM",
        "岗位类型": "BACKEND",
        "标签": "编程题,数组,反转"
    },
    {
        "题目标题": "判断链表是否有环",
        "题目内容": "给定一个链表，判断链表中是否存在环。",
        "函数名称": "public boolean hasCycle(ListNode head) {    // 请在此实现判断链表是否有环的算法    return false;}",
        "参数类型": "ListNode",
        "返回值类型": "boolean",
        "测试用例输入": "1 -> 2 -> 3 -> 4 -> 2 （存在环，4指向2）",
        "测试用例输出": "true",
        "Java代码模板": "class ListNode {    int val;    ListNode next;    ListNode(int x) {        val = x;        next = null;    }}public boolean hasCycle(ListNode head) {    // 请在此补充完整判断链表是否有环的代码逻辑    return false;}",
        "Python代码模板": "class ListNode:    def __init__(self, x):        self.val = x        self.next = None\ndef hasCycle(head):    // 请在此补充完整判断链表是否有环的代码逻辑    return False",
        "C++代码模板": "struct ListNode {    int val;    ListNode *next;    ListNode(int x) : val(x), next(NULL) {}};bool hasCycle(ListNode* head) {    // 请在此补充完整判断链表是否有环的代码逻辑    return false;}",
        "C代码模板": "struct ListNode {    int val;    struct ListNode *next;};bool hasCycle(struct ListNode* head) {    // 请在此补充完整判断链表是否有环的代码逻辑    return false;}",
        "Java标准答案": "class ListNode {    int val;    ListNode next;    ListNode(int x) {        val = x;        next = null;    }}public boolean hasCycle(ListNode head) {    if (head == null) {        return false;    }    ListNode slow = head;    ListNode fast = head.next;    while (fast != null && fast.next != null) {        if (slow == fast) {            return true;        }        slow = slow.next;        fast = fast.next.next;    }    return false;}",
        "Python标准答案": "class ListNode:    def __init__(self, x):        self.val = x        self.next = None\ndef hasCycle(head):    if not head:        return False    slow, fast = head, head.next    while fast and fast.next:        if slow == fast:            return True        slow = slow.next        fast = fast.next.next    return False",
        "C++标准答案": "struct ListNode {    int val;    ListNode *next;    ListNode(int x) : val(x), next(NULL) {}};bool hasCycle(ListNode* head) {    if (head == NULL) {        return false;    }    ListNode* slow = head;    ListNode* fast = head->next;    while (fast != NULL && fast->next != NULL) {        if (slow == fast) {            return true;        }        slow = slow->next;        fast = fast->next->next;    }    return false;}",
        "C标准答案": "struct ListNode {    int val;    struct ListNode *next;};bool hasCycle(struct ListNode* head) {    if (head == NULL) {        return false;    }    struct ListNode* slow = head;    struct ListNode* fast = head->next;    while (fast != NULL && fast->next != NULL) {        if (slow == fast) {            return true;        }        slow = slow->next;        fast = fast->next->next;    }    return false;}",
        "时间限制(ms)": 1000,
        "内存限制(MB)": 128,
        "难度": "MEDIUM",
        "岗位类型": "BACKEND",
        "标签": "编程题,链表,环检测"
    },

            {
                "题目标题": "计算字符串中单词的数量",
                "题目内容": "给定一个字符串，统计其中单词的数量。单词由空格分隔。",
                "函数名称": "public int countWords(String str) {    // 请在此实现计算字符串中单词数量的算法    return 0;}",
                "参数类型": "String",
                "返回值类型": "int",
                "测试用例输入": "\"Hello world, this is a test\"",
                "测试用例输出": "6",
                "Java代码模板": "public int countWords(String str) {    // 请在此补充完整计算字符串中单词数量的代码逻辑    return 0;}",
                "Python代码模板": "def countWords(str):    // 请在此补充完整计算字符串中单词数量的代码逻辑    return 0",
                "C++代码模板": "#include <string>int countWords(const std::string& str) {    // 请在此补充完整计算字符串中单词数量的代码逻辑    return 0;}",
                "C代码模板": "#include <string.h>int countWords(const char* str) {    // 请在此补充完整计算字符串中单词数量的代码逻辑    return 0;}",
                "Java标准答案": "public int countWords(String str) {    if (str == null || str.isEmpty()) {        return 0;    }    String[] words = str.trim().split("\\s+");    return words.length;}",
                "Python标准答案": "def countWords(str):    return len(str.split())",
                "C++标准答案": "#include <string>#include <sstream>int countWords(const std::string& str) {    std::istringstream iss(str);    return std::distance(std::istream_iterator<std::string>(iss), std::istream_iterator<std::string>());}",
                "C标准答案": "#include <string.h>int countWords(const char* str) {    if (str == NULL || *str == '\\0') {        return 0;    }    int count = 0;    int inWord = 0;    while (*str) {        if (*str == ' ' || *str == '\\t' || *str == '\\n') {            inWord = 0;        } else if (!inWord) {            inWord = 1;            count++;        }        str++;    }    return count;}",
                "时间限制(ms)": 1000,
                "内存限制(MB)": 128,
                "难度": "EASY",
                "岗位类型": "FRONTEND",
                "标签": "编程题,字符串,单词计数"
            },
            {
                "题目标题": "合并两个有序数组",
                "题目内容": "给定两个有序整数数组，将它们合并成一个新的有序数组。",
                "函数名称": "public int[] mergeSortedArrays(int[] nums1, int[] nums2) {    // 请在此实现合并两个有序数组的算法    return new int[0];}",
                "参数类型": "int[], int[]",
                "返回值类型": "int[]",
                "测试用例输入": "[1, 3, 5], [2, 4, 6]",
                "测试用例输出": "[1, 2, 3, 4, 5, 6]",
                "Java代码模板": "public int[] mergeSortedArrays(int[] nums1, int[] nums2) {    // 请在此补充完整合并两个有序数组的代码逻辑    return new int[0];}",
                "Python代码模板": "def mergeSortedArrays(nums1, nums2):    // 请在此补充完整合并两个有序数组的代码逻辑    return []",
                "C++代码模板": "#include <vector>std::vector<int> mergeSortedArrays(const std::vector<int>& nums1, const std::vector<int>& nums2) {    // 请在此补充完整合并两个有序数组的代码逻辑    return std::vector<int>();}",
                "C代码模板": "#include <stdlib.h>int* mergeSortedArrays(int* nums1, int nums1Size, int* nums2, int nums2Size, int* returnSize) {    // 请在此补充完整合并两个有序数组的代码逻辑    return NULL;}",
                "Java标准答案": "public int[] mergeSortedArrays(int[] nums1, int[] nums2) {    int[] merged = new int[nums1.length + nums2.length];    int i = 0, j = 0, k = 0;    while (i < nums1.length && j < nums2.length) {        if (nums1[i] < nums2[j]) {            merged[k++] = nums1[i++];        } else {            merged[k++] = nums2[j++];        }    }    while (i < nums1.length) {        merged[k++] = nums1[i++];    }    while (j < nums2.length) {        merged[k++] = nums2[j++];    }    return merged;}",
                "Python标准答案": "def mergeSortedArrays(nums1, nums2):    merged = []    i = j = 0    while i < len(nums1) and j < len(nums2):        if nums1[i] < nums2[j]:            merged.append(nums1[i])            i += 1        else:            merged.append(nums2[j])            j += 1    merged.extend(nums1[i:])    merged.extend(nums2[j:])    return merged",
                "C++标准答案": "#include <vector>std::vector<int> mergeSortedArrays(const std::vector<int>& nums1, const std::vector<int>& nums2) {    std::vector<int> merged(nums1.size() + nums2.size());    int i = 0, j = 0, k = 0;    while (i < nums1.size() && j < nums2.size()) {        if (nums1[i] < nums2[j]) {            merged[k++] = nums1[i++];        } else {            merged[k++] = nums2[j++];        }    }    while (i < nums1.size()) {        merged[k++] = nums1[i++];    }    while (j < nums2.size()) {        merged[k++] = nums2[j++];    }    return merged;}",
                "C标准答案": "#include <stdlib.h>int* mergeSortedArrays(int* nums1, int nums1Size, int* nums2, int nums2Size, int* returnSize) {    *returnSize = nums1Size + nums2Size;    int* merged = (int*)malloc(*returnSize * sizeof(int));    int i = 0, j = 0, k = 0;    while (i < nums1Size && j < nums2Size) {        if (nums1[i] < nums2[j]) {            merged[k++] = nums1[i++];        } else {            merged[k++] = nums2[j++];        }    }    while (i < nums1Size) {        merged[k++] = nums1[i++];    }    while (j < nums2Size) {        merged[k++] = nums2[j++];    }    return merged;}",
                "时间限制(ms)": 1000,
                "内存限制(MB)": 128,
                "难度": "MEDIUM",
                "岗位类型": "BACKEND",
                "标签": "编程题,数组,合并排序"
            },
            {
                "题目标题": "计算二叉树的深度",
                "题目内容": "给定一个二叉树，计算其最大深度。最大深度是指从根节点到最远叶子节点的最长路径上的节点数。",
                "函数名称": "public int maxDepth(TreeNode root) {    // 请在此实现计算二叉树深度的算法    return 0;}",
                "参数类型": "TreeNode",
                "返回值类型": "int",
                "测试用例输入": "二叉树: [3,9,20,null,null,15,7]",
                "测试用例输出": "3",
                "Java代码模板": "class TreeNode {    int val;    TreeNode left;    TreeNode right;    TreeNode(int x) { val = x; }}public int maxDepth(TreeNode root) {    // 请在此补充完整计算二叉树深度的代码逻辑    return 0;}",
                "Python代码模板": "class TreeNode:    def __init__(self, x):        self.val = x        self.left = None        self.right = None\ndef maxDepth(root):    // 请在此补充完整计算二叉树深度的代码逻辑    return 0",
                "C++代码模板": "struct TreeNode {    int val;    TreeNode *left;    TreeNode *right;    TreeNode(int x) : val(x), left(NULL), right(NULL) {}};int maxDepth(TreeNode* root) {    // 请在此补充完整计算二叉树深度的代码逻辑    return 0;}",
                "C代码模板": "#include <stdlib.h>struct TreeNode {    int val;    struct TreeNode *left;    struct TreeNode *right;};int maxDepth(struct TreeNode* root) {    // 请在此补充完整计算二叉树深度的代码逻辑    return 0;}",
                "Java标准答案": "class TreeNode {    int val;    TreeNode left;    TreeNode right;    TreeNode(int x) { val = x; }}public int maxDepth(TreeNode root) {    if (root == null) {        return 0;    }    int leftDepth = maxDepth(root.left);    int rightDepth = maxDepth(root.right);    return Math.max(leftDepth, rightDepth) + 1;}",
                "Python标准答案": "class TreeNode:    def __init__(self, x):        self.val = x        self.left = None        self.right = None\ndef maxDepth(root):    if not root:        return 0    return max(maxDepth(root.left), maxDepth(root.right)) + 1",
                "C++标准答案": "struct TreeNode {    int val;    TreeNode *left;    TreeNode *right;    TreeNode(int x) : val(x), left(NULL), right(NULL) {}};int maxDepth(TreeNode* root) {    if (root == NULL) {        return 0;    }    int leftDepth = maxDepth(root->left);    int rightDepth = maxDepth(root->right);    return std::max(leftDepth, rightDepth) + 1;}",
                "C标准答案": "#include <stdlib.h>struct TreeNode {    int val;    struct TreeNode *left;    struct TreeNode *right;};int maxDepth(struct TreeNode* root) {    if (root == NULL) {        return 0;    }    int leftDepth = maxDepth(root->left);    int rightDepth = maxDepth(root->right);    return (leftDepth > rightDepth ? leftDepth : rightDepth) + 1;}",
                "时间限制(ms)": 1000,
                "内存限制(MB)": 128,
                "难度": "MEDIUM",
                "岗位类型": "BACKEND",
                "标签": "编程题,二叉树,深度计算"
            },
            {
                "题目标题": "检查括号匹配",
                "题目内容": "给定一个只包含括号的字符串，检查括号是否匹配。括号匹配的条件是每个左括号都有对应的右括号，并且括号的嵌套顺序正确。",
                "函数名称": "public boolean isValid(String s) {    // 请在此实现检查括号匹配的算法    return false;}",
                "参数类型": "String",
                "返回值类型": "boolean",
                "测试用例输入": "\"()[]{}\"",
                "测试用例输出": "true",
                "Java代码模板": "import java.util.Stack;public boolean isValid(String s) {    // 请在此补充完整检查括号匹配的代码逻辑    return false;}",
                "Python代码模板": "def isValid(s):    // 请在此补充完整检查括号匹配的代码逻辑    return False",
                "C++代码模板": "#include <string>#include <stack>bool isValid(const std::string& s) {    // 请在此补充完整检查括号匹配的代码逻辑    return false;}",
                "C代码模板": "#include <stdbool.h>#include <string.h>bool isValid(const char* s) {    // 请在此补充完整检查括号匹配的代码逻辑    return false;}",
                "Java标准答案": "import java.util.Stack;public boolean isValid(String s) {    Stack<Character> stack = new Stack<>();    for (char c : s.toCharArray()) {        if (c == '(' || c == '[' || c == '{') {            stack.push(c);        } else {            if (stack.isEmpty()) {                return false;            }            char top = stack.pop();            if ((c == ')' && top != '(') || (c == ']' && top != '[') || (c == '}' && top != '{')) {                return false;            }        }    }    return stack.isEmpty();}",
                "Python标准答案": "def isValid(s):    stack = []    mapping = {')': '(', ']': '[', '}': '{'}    for char in s:        if char in mapping:            top_element = stack.pop() if stack else '#'            if mapping[char] != top_element:                return False        else:            stack.append(char)    return not stack",
                "C++标准答案": "#include <string>#include <stack>bool isValid(const std::string& s) {    std::stack<char> stack;    for (char c : s) {        if (c == '(' || c == '[' || c == '{') {            stack.push(c);        } else {            if (stack.empty()) {                return false;            }            char top = stack.top();            stack.pop();            if ((c == ')' && top != '(') || (c == ']' && top != '[') || (c == '}' && top != '{')) {                return false;            }        }    }    return stack.empty();}",
                "C标准答案": "#include <stdbool.h>#include <string.h>bool isValid(const char* s) {    int len = strlen(s);    if (len == 0) return true;    if (len % 2 != 0) return false;    char stack[len];    int top = -1;    for (int i = 0; i < len; i++) {        if (s[i] == '(' || s[i] == '[' || s[i] == '{') {            stack[++top] = s[i];        } else {            if (top == -1) return false;            char c = stack[top--];            if ((s[i] == ')' && c != '(') || (s[i] == ']' && c != '[') || (s[i] == '}' && c != '{')) {                return false;            }        }    }    return top == -1;}",
                "时间限制(ms)": 1000,
                "内存限制(MB)": 128,
                "难度": "MEDIUM",
                "岗位类型": "BACKEND",
                "标签": "编程题,字符串,栈"
            }
        ]

        # 将新的编程题数据转换为DataFrame
        new_df = pd.DataFrame(new_questions)

        # 保存为新的CSV文件
        new_csv_path = '/mnt/666_new_questions.csv'
        new_df.to_csv(new_csv_path, index=False)
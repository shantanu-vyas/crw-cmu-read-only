function y = imageName(image)
%% Obstacle detection on the water surface
% image is the photo file to be read
%I = imread(image);
[X Y] = size(image);
%figure, imshow(image);

%% Crop the image to the ROI
%cropped = imcrop(image,[0 950 Y Y/9.5]);
% image,x ,y , width of box, height of box
cropped = imcrop(image,[0 0 Y Y/5.5]);
[X Y] = size(cropped);
%iptsetpref ImshowBorder Light
%figure, imshow(cropped);

%% Evaluate and find the horizon line in the picture. This will gives us a
% divition where above the horizon is (most likely) to be ground, and
% below is (most likely) to be water
% draw a white line that will represent the horizon
%for j = Y/3:-1:1
 %   cropped(X/2,j,2) = 0.1;
%end        

%% Convert from RGB to HSV to control the brightness of the objects.
% work with reflexion
cropped = rgb2hsv(cropped);
%figure, imshow(cropped);
%for i = 300:-1:1               % X (more or less) = 820 pixels
%   for j = Y/3:-1:1
        %cropped(i,j,2) = 0;
        %cropped(i,j,3) = 1;
    %end
%end 
I = cropped;
for i = X:-1:1          
    for j = Y/3:-1:1
        % look on how bright the object is
        if I(i,j,3) > 0.7
        %The HSV values vary between 0 and 1
            %I(i,j,1) = 0;     %H (color)
            %I(i,j,2) = 0.1;     %S (color intensity)
            I(i,j,3) = 0.8;       %V (brightness)
        end
    end
end
cropped = I;
%cropped = hsv2rgb(cropped);
%figure, imshow(cropped);

%% Grab a random patch of water below the horizon and compare every other
% pixel against it
% water sample
for i = 1:150
    for j = 1:150
        % water patch sample (150 X 150 matirx)
        waterTraining(i,j,1) = cropped(i+949,j+949,1);
        %waterTraining(i,j,2) = cropped(i+949,j+949,2);
        %waterTraining(i,j,3) = cropped(i+949,j+949,3);
        
        %path is green (this is for me to know where the water patch is)
        %cropped(i+949,j+949,1) = 173;
        %cropped(i+949,j+949,2) = 255;
        %cropped(i+949,j+949,3) = 47;
    end
end
% ground sample
for i = 1:150
    for j = 1:150
        % water patch sample (150 X 150 matirx)
        groundTraining(i,j,1) = cropped(i+149,j+849,1);
        %groundTraining(i,j,2) = cropped(i+149,j+849,2);
        %groundTraining(i,j,3) = cropped(i+149,j+849,3);
        
        %patch is red (this is for me to know where the ground patch is)
        %cropped(i+149,j+849,1) = 255;
        %cropped(i+149,j+849,2) = 0;
        %cropped(i+149,j+849,3) = 0;
    end
end
%get the mean or color average for all the pixels in that patch of water
%waterPatch = [mean2(waterPatch(:,:,1)),mean2(waterPatch(:,:,2)),mean2(waterPatch(:,:,3))]
% compare every pixel against that patch
[X Y] = size(cropped);
%get a random sample
for i = 1:150          
    for j = 1:150
        % random sample taken from the picture. Must be determined whether
        % is is water or ground
        sample(i,j,1) = cropped(i+950,j+100,1);
        %sample(i,j,2) = cropped(i+100,j+100,2);
        %sample(i,j,3) = cropped(i+100,j+100,3);
    end
end
%[x y] = size(group)
%[x y] = size(waterTraining)
%[x y] = size(groundTraining)
%[x y] = size(sample)
% add the classes after each classification. For example, if the sum of the
% from the water patch are bigger than the ground patch, then the pixel
% belongs to the water (sort of inefficient)
[waterNeighbors waterDistance] = kNearestNeighbors(sample,waterTraining,50);
waterSum = sum(waterDistance);
[groundNeighbors groundDistance] = kNearestNeighbors(sample,groundTraining,50);
groundSum = sum(groundDistance);
if groundSum < waterSum
    for i = 950:1100
        for j = 1:150
            % mark non-water samples as red
            cropped(i,j,1) = 0;
            cropped(i,j,2) = 1;
            cropped(i,j,3) = 1;
        end
    end
else
    for i = 950:1100
        for j = 1:150
            % green otherwise
            cropped(i,j,1) = 85/255;
            cropped(i,j,2) = 1;
            cropped(i,j,3) = 128/255;
        end
    end
end
cropped = hsv2rgb(cropped);
figure, imshow(cropped);

%% Segmenting image with a theshhold
obstacles = rgb2gray(cropped);
%filter the image to reduce some noise/reflectance?
obstacles = wiener2(obstacles, [2 1]);
threshold = 50;            %based on object brightness/darkness
%threshold = graythresh(obstacles);
%obstacles = im2bw(obstacles, threshold/255); %3.8
%figure, imshow(obstacles);
%finding edges with the 'canny'
obstacles = edge(obstacles, 'canny', 60/255, 1.5);
% Do a "hole fill" to get rid of any background pixels inside the blobs.
obstacles = imfill(obstacles, 'holes');
%figure, imshow(obstacles); 
%eliminate from the picture anything fewer than 200 pixels
obstacles = bwareaopen(obstacles, 50);

% fill a gap in the pen's cap
%se = strel('disk',5);
%obstacles = imclose(obstacles,se);

%% Clean Image from objects touching borders
%obstacles = imclearborder(obstacles);
%figure, imshow(obstacles);

%% Find Obstacles
% bwboundaries() returns a cell array, where each cell contains the row/column coordinates for the obstacle in the image.
[B,L] = bwboundaries(obstacles,'noholes');
numRegions = max(L(:))
% Label each blob with a specific color.
%labeledImage = bwlabeln(L, 8);
%figure, imshow(label2rgb(labeledImage));

%% Region Extraction - connected region properties
stats = regionprops(L,'all');

%% Analyse Eccentricity and Area
% May have to look for objects of different shapes.
% for that may add other props other than 'Eccentricity' and 'Area'
eccentricityShape = [stats.Eccentricity];
areaShape = [stats.Area];
eccNum = find(eccentricityShape < 1);
areaNum = find(areaShape > 100);

%% Display selected images
% Display region boundaries over cropped images
% bound obstacles of certain areas and eccentricity
for index=1:length(areaNum)
    bound = B{areaNum(index)};
    line(bound(:,2),bound(:,1),'Color','r','LineWidth',2)
end
% bound obstacles of certain eccentricity
%for index=1:length(eccNum)
 %   bound = B{eccNum(index)};
  %  line(bound(:,2),bound(:,1),'Color','r','LineWidth',2)
%end